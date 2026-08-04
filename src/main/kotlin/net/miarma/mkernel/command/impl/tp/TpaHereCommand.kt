package net.miarma.mkernel.command.impl.tp

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.annotation.RequiresModule
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.service.impl.TeleportService
import net.miarma.mkernel.common.teleport.TpaType
import net.miarma.mkernel.util.CommandUtil.checkModule
import net.miarma.mkernel.util.PlayerUtil

@Singleton
@RequiresModule(ConfigKeys.Modules.Teleport.MAIN)
class TpaHereCommand @Inject constructor(
    private val plugin: MKernel,
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val teleportService: TeleportService,
    private val moduleLoader: ModuleLoader
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.TpaHere.NAME)) {
            checkModule(moduleLoader, configService, messageService, ConfigKeys.Modules.Teleport.MAIN)
            withArguments(PlayerProfileArgument(configService.getString(ConfigKeys.Arguments.PLAYER)))
            withPermission(configService.getString(ConfigKeys.Commands.TpaHere.PERM))
            withFullDescription(configService.getString(ConfigKeys.Commands.TpaHere.DESC))
            withUsage(configService.getString(ConfigKeys.Commands.TpaHere.USAGE))
            playerExecutor { sender, args ->
                val target = PlayerUtil.fromArg(args[0])
                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.PLAYER_NOT_FOUND)).withPrefix().send(sender)
                    return@playerExecutor
                }

                if (target == sender) {
                    messageService.builder(configService.getString(ConfigKeys.Messages.Teleport.Errors.CANT_TP_SELF)).withPrefix().send(sender)
                    return@playerExecutor
                }

                plugin.launchSync {
                    val request = teleportService.getTpaRequest(sender, target)
                    if (request != null) {
                        messageService.builder(configService.getString(ConfigKeys.Messages.Teleport.Errors.REQ_ALREADY_SENT)).withPrefix().send(sender)
                    } else {
                        teleportService.addRequest(sender, target, TpaType.TPA_HERE)
                        messageService.builder(configService.getString(ConfigKeys.Commands.TpaHere.MSG_TO)).withPrefix().tag("target", target.name).send(sender)
                        messageService.builder(configService.getString(ConfigKeys.Commands.TpaHere.MSG_FROM)).withPrefix().forPlayer(target).tag("sender", sender.name).send(target)
                    }
                }
            }
        }
    }
}