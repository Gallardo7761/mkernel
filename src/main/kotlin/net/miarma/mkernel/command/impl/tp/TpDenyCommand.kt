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
import net.miarma.mkernel.common.teleport.TpaRequest
import net.miarma.mkernel.util.CommandUtil.checkModule
import net.miarma.mkernel.util.PlayerUtil

@Singleton
@RequiresModule(ConfigKeys.Modules.Teleport.MAIN)
class TpDenyCommand @Inject constructor(
    private val plugin: MKernel,
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val teleportService: TeleportService,
    private val moduleLoader: ModuleLoader
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.TpDeny.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Teleport.MAIN)
            withOptionalArguments(PlayerProfileArgument(configService.getString(ConfigKeys.Arguments.PLAYER)))
            withPermission(configService.getString(ConfigKeys.Commands.TpDeny.PERM))
            withFullDescription(configService.getString(ConfigKeys.Commands.TpDeny.DESC))
            withUsage(configService.getString(ConfigKeys.Commands.TpDeny.USAGE))
            playerExecutor { sender, args ->
                plugin.launchSync {
                    if (args[0] == null) {
                        val request = teleportService.getIncomingTpaRequest(sender)
                        if (request == null) {
                            messageService.builder(configService.getString(ConfigKeys.Messages.Teleport.Errors.NO_REQ_FOUND)).withPrefix().send(sender)
                            return@launchSync
                        }
                        denyRequest(sender, request.from, request)
                    } else {
                        val target = PlayerUtil.fromArg(args[0])
                        if (target == null || !target.isOnline) {
                            messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.PLAYER_NOT_FOUND)).withPrefix().send(sender)
                            return@launchSync
                        }

                        val request = teleportService.getTpaRequest(target, sender)
                        if (request == null) {
                            messageService.builder(configService.getString(ConfigKeys.Messages.Teleport.Errors.NO_REQ_FOUND)).withPrefix().send(sender)
                            return@launchSync
                        }
                        denyRequest(sender, target, request)
                    }
                }
            }
        }
    }

    private fun denyRequest(sender: org.bukkit.entity.Player, target: org.bukkit.entity.Player, request: TpaRequest) {
        teleportService.removeRequest(request)
        messageService.builder(configService.getString(ConfigKeys.Commands.TpDeny.MSG_DENIED_TARGET)).withPrefix().forPlayer(target).tag("sender", sender.name).send(target)
        messageService.builder(configService.getString(ConfigKeys.Commands.TpDeny.MSG_DENIED)).withPrefix().tag("target", target.name).send(sender)
    }
}