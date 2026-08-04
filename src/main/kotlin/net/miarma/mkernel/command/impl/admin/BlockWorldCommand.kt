package net.miarma.mkernel.command.impl.admin

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.DatabaseService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.task.LocationTrackerTask
import org.bukkit.Bukkit

@Singleton
class BlockWorldCommand @Inject constructor(
    private val plugin: MKernel,
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val databaseService: DatabaseService,
    private val locationTrackerTask: LocationTrackerTask
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.BlockWorld.NAME)) {
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
                    val isBlocked = databaseService.isWorldBlocked(worldName)

                    if (isBlocked) {
                        databaseService.setWorldBlocked(worldName, false)
                        messageService.builder(configService.getString(ConfigKeys.Commands.BlockWorld.MSG_UNBLOCKED)).withPrefix().tag("world", worldName).send(sender)
                    } else {
                        databaseService.setWorldBlocked(worldName, true)

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