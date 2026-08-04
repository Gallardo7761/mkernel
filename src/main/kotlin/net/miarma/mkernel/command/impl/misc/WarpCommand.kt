package net.miarma.mkernel.command.impl.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.annotation.RequiresModule
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.DatabaseService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.CommandUtil.checkModule

@Singleton
@RequiresModule(ConfigKeys.Modules.Teleport.MAIN)
class WarpCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val databaseService: DatabaseService,
    private val moduleLoader: ModuleLoader
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Warp.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Teleport.MAIN)
            withPermission(configService.getString(ConfigKeys.Commands.Warp.PERM))
            withFullDescription(configService.getString(ConfigKeys.Commands.Warp.DESC))
            withUsage(configService.getString(ConfigKeys.Commands.Warp.USAGE))
            playerExecutor { sender, _ ->
                val warps = databaseService.getWarpObjects(sender)

                if (warps.isEmpty()) {
                    messageService.builder(configService.getString(ConfigKeys.Commands.Warp.MSG_NO_WARPS)).withPrefix().send(sender)
                } else {
                    val warpList = warps.joinToString("\n") { it.toFormattedMessage() }
                    messageService.builder(configService.getString(ConfigKeys.Commands.Warp.MSG_LIST)).withPrefix().tag("warps", warpList).send(sender)
                }
            }
            withSubcommand(CommandAPICommand(configService.getString(ConfigKeys.Commands.Warp.Add.NAME)).apply {
                withPermission(configService.getString(ConfigKeys.Commands.Warp.Add.PERM))
                withArguments(StringArgument(configService.getString(ConfigKeys.Arguments.WARP_NAME)))
                withUsage(configService.getString(ConfigKeys.Commands.Warp.Add.USAGE))
                playerExecutor { sender, args ->
                    val warpName = args[0] as String
                    val count = databaseService.getWarpCount(sender)

                    if (count >= configService.getInt(ConfigKeys.Settings.Teleport.MAX_WARPS)) {
                        messageService.builder(configService.getString(ConfigKeys.Messages.Teleport.Errors.MAX_WARPS)).withPrefix().send(sender)
                        return@playerExecutor
                    }

                    val exists = databaseService.warpExists(sender, warpName)
                    if (exists) {
                        messageService.builder(configService.getString(ConfigKeys.Commands.Warp.Add.MSG_EXISTS))
                            .withPrefix().tag("warp", warpName).send(sender)
                        return@playerExecutor
                    }

                    databaseService.createWarp(sender, warpName, sender.location)
                    messageService.builder(configService.getString(ConfigKeys.Commands.Warp.Add.MSG_ADDED))
                        .withPrefix().tag("warp", warpName).send(sender)
                }
            })
            withSubcommand(CommandAPICommand(configService.getString(ConfigKeys.Commands.Warp.Remove.NAME)).apply {
                withPermission(configService.getString(ConfigKeys.Commands.Warp.Remove.PERM))
                withArguments(
                    StringArgument(configService.getString(ConfigKeys.Arguments.WARP_NAME))
                        .replaceSuggestions(ArgumentSuggestions.strings { info ->
                            val player = info.sender as? org.bukkit.entity.Player ?: return@strings arrayOf()
                            databaseService.getWarpObjects(player).map { it.alias }.toTypedArray()
                        })
                )
                withUsage(configService.getString(ConfigKeys.Commands.Warp.Remove.USAGE))
                playerExecutor { sender, args ->
                    val warpName = args[0] as String
                    val exists = databaseService.warpExists(sender, warpName)

                    if (exists) {
                        databaseService.deleteWarp(sender, warpName)
                        messageService.builder(configService.getString(ConfigKeys.Commands.Warp.Remove.MSG_REMOVED))
                            .withPrefix().tag("warp", warpName).send(sender)
                    } else {
                        messageService.builder(configService.getString(ConfigKeys.Commands.Warp.Remove.MSG_NOT_FOUND))
                            .withPrefix().tag("warp", warpName).send(sender)
                    }
                }
            })
        }
    }
}