package net.miarma.mkernel.command.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.DatabaseService
import net.miarma.mkernel.common.service.impl.MessageService

@Singleton
class WarpCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val databaseService: DatabaseService
) {
    fun register() {
        commandAPICommand(configService.getString("commands.warp.name")) {
            withPermission(configService.getString("commands.warp.permission"))
            withFullDescription(configService.getString("commands.warp.description"))
            withUsage(configService.getString("commands.warp.usage"))
            playerExecutor { sender, _ ->
                databaseService.getWarpObjects(sender) { warps ->
                    if (warps.isEmpty()) {
                        messageService.builder(configService.getString("commands.warp.messages.noWarpsStored")).withPrefix().send(sender)
                    } else {
                        val warpList = warps.joinToString("\n") { it.toFormattedMessage() }
                        messageService.builder(configService.getString("commands.warp.messages.warpList")).withPrefix().tag("warps", warpList).send(sender)
                    }
                }
            }
            withSubcommand(CommandAPICommand(configService.getString("commands.warp.subcommands.add.name")).apply {
                withPermission(configService.getString("commands.warp.subcommands.add.permission"))
                withArguments(StringArgument(configService.getString("arguments.warpName")))
                withUsage(configService.getString("commands.warp.subcommands.add.usage"))
                playerExecutor { sender, args ->
                    val warpName = args[0] as String
                    databaseService.getWarpCount(sender) { count ->
                        if (count >= configService.getInt("config.values.maxWarps")) {
                            messageService.builder(configService.getString("language.errors.maxWarpsReached")).withPrefix().send(sender)
                            return@getWarpCount
                        }
                        databaseService.warpExists(sender, warpName) { exists ->
                            if (exists) {
                                messageService.builder(configService.getString("commands.warp.subcommands.add.messages.warpAlreadyExists"))
                                    .withPrefix().tag("warp", warpName).send(sender)
                                return@warpExists
                            }
                            databaseService.createWarp(sender, warpName, sender.location)
                            messageService.builder(configService.getString("commands.warp.subcommands.add.messages.warpAdded"))
                                .withPrefix().tag("warp", warpName).send(sender)
                        }
                    }
                }
            })
            withSubcommand(CommandAPICommand(configService.getString("commands.warp.subcommands.remove.name")).apply {
                withPermission(configService.getString("commands.warp.subcommands.remove.permission"))
                withArguments(StringArgument(configService.getString("arguments.warpName")))
                withUsage(configService.getString("commands.warp.subcommands.remove.usage"))
                playerExecutor { sender, args ->
                    val warpName = args[0] as String
                    databaseService.warpExists(sender, warpName) { exists ->
                        if (exists) {
                            databaseService.deleteWarp(sender, warpName)
                            messageService.builder(configService.getString("commands.warp.subcommands.remove.messages.warpRemoved"))
                                .withPrefix().tag("warp", warpName).send(sender)
                        } else {
                            messageService.builder(configService.getString("commands.warp.subcommands.remove.messages.warpNotFound"))
                                .withPrefix().tag("warp", warpName).send(sender)
                        }
                    }
                }
            })
        }
    }
}