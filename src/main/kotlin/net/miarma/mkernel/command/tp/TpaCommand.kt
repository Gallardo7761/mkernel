package net.miarma.mkernel.command.tp

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.service.impl.TeleportService
import net.miarma.mkernel.common.teleport.TpaType
import net.miarma.mkernel.util.PlayerUtil

@Singleton
class TpaCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val teleportService: TeleportService
) {
    fun register() {
        commandAPICommand(configService.getString("commands.tpa.name")) {
            withArguments(PlayerProfileArgument(configService.getString("arguments.player")))
            withPermission(configService.getString("commands.tpa.permission"))
            withFullDescription(configService.getString("commands.tpa.description"))
            withUsage(configService.getString("commands.tpa.usage"))
            playerExecutor { sender, args ->
                val target = PlayerUtil.fromArg(args[0])
                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString("language.errors.playerNotFound")).withPrefix().send(sender)
                    return@playerExecutor
                }

                if (target == sender) {
                    messageService.builder(configService.getString("language.errors.cantTeleportToYourself")).withPrefix().send(sender)
                    return@playerExecutor
                }

                teleportService.getTpaRequest(sender, target) { request ->
                    if (request != null) {
                        messageService.builder(configService.getString("language.errors.requestAlreadySent")).withPrefix().send(sender)
                    } else {
                        teleportService.addRequest(sender, target, TpaType.TPA)
                        messageService.builder(configService.getString("commands.tpa.messages.tpaToPlayer")).withPrefix().tag("target", target.name).send(sender)
                        messageService.builder(configService.getString("commands.tpa.messages.tpaFromPlayer")).withPrefix().forPlayer(target).tag("sender", sender.name).send(target)
                    }
                }
            }
        }
    }
}