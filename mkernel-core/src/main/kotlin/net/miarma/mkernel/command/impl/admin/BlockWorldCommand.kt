package net.miarma.mkernel.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.annotation.RequiresModule
import net.miarma.mkernel.api.common.ICommand
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.dao.WorldDao
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.task.impl.LocationTrackerTask
import net.miarma.mkernel.util.CommandUtil.checkModule
import org.bukkit.Bukkit

@Singleton
@RequiresModule(ConfigKeys.Modules.World.MAIN)
class BlockWorldCommand @Inject constructor(
    private val plugin: MKernel,
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val worldDao: WorldDao,
    private val locationTrackerTask: LocationTrackerTask,
    private val moduleLoader: ModuleLoader
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.BlockWorld.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.World.MAIN)
            withArguments(
                StringArgument(configService.getString(ConfigKeys.Arguments.WORLD))
                    .replaceSuggestions(ArgumentSuggestions.strings { _ ->
                        Bukkit.getWorlds().map { it.name }.toTypedArray()
                    })
            )
            withAliases(*configService.getStringList(ConfigKeys.Commands.BlockWorld.ALIASES).toTypedArray())
            withFullDescription(configService.getString(ConfigKeys.Commands.BlockWorld.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.BlockWorld.PERM))
            withUsage(configService.getString(ConfigKeys.Commands.BlockWorld.USAGE))
            playerExecutor { sender, args ->
                val worldName = args[0] as? String
                if (worldName == null || Bukkit.getWorld(worldName) == null) {
                    messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.INVALID_ARGUMENT)).withPrefix().send(sender)
                    return@playerExecutor
                }

                plugin.launchSync {
                    val isBlocked = worldDao.isWorldBlocked(worldName)

                    if (isBlocked) {
                        worldDao.setWorldBlocked(worldName, false)
                        messageService.builder(configService.getString(ConfigKeys.Commands.BlockWorld.MSG_UNBLOCKED)).withPrefix().tag("world", worldName).send(sender)
                    } else {
                        worldDao.setWorldBlocked(worldName, true)

                        Bukkit.getWorld(worldName)?.players?.forEach { p ->
                            locationTrackerTask.getPlayerRealTimeLocation(p)?.let { loc ->
                                p.teleportAsync(loc)
                            }
                        }
                        messageService.builder(configService.getString(ConfigKeys.Commands.BlockWorld.MSG_BLOCKED)).withPrefix().tag("world", worldName).send(sender)
                    }
                }
            }
        }
    }
}