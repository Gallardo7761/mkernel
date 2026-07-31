package net.miarma.mkernel.command.tp

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.service.impl.TeleportService
import net.miarma.mkernel.common.teleport.TpaRequest
import net.miarma.mkernel.util.PlayerUtil

@Singleton
class TpDenyCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val teleportService: TeleportService
) {
    fun register() {
        commandAPICommand(configService.getString("commands.tpdeny.name")) {
            withOptionalArguments(PlayerProfileArgument(configService.getString("arguments.player")))
            withPermission(configService.getString("commands.tpdeny.permission"))
            withFullDescription(configService.getString("commands.tpdeny.description"))
            withUsage(configService.getString("commands.tpdeny.usage"))
            playerExecutor { sender, args ->
                if (args[0] == null) {
                    teleportService.getIncomingTpaRequest(sender) { request ->
                        if (request == null) {
                            messageService.builder(configService.getString("language.errors.noRequestFound")).withPrefix().send(sender)
                            return@getIncomingTpaRequest
                        }
                        denyRequest(sender, request.from, request)
                    }
                } else {
                    val target = PlayerUtil.fromArg(args[0])
                    if (target == null || !target.isOnline) {
                        messageService.builder(configService.getString("language.errors.playerNotFound")).withPrefix().send(sender)
                        return@playerExecutor
                    }

                    teleportService.getTpaRequest(target, sender) { request ->
                        if (request == null) {
                            messageService.builder(configService.getString("language.errors.noRequestFound")).withPrefix().send(sender)
                            return@getTpaRequest
                        }
                        denyRequest(sender, target, request)
                    }
                }
            }
        }
    }

    private fun denyRequest(sender: org.bukkit.entity.Player, target: org.bukkit.entity.Player, request: TpaRequest) {
        teleportService.removeRequest(request)
        messageService.builder(configService.getString("commands.tpdeny.messages.deniedToTarget")).withPrefix().forPlayer(target).tag("sender", sender.name).send(target)
        messageService.builder(configService.getString("commands.tpdeny.messages.denied")).withPrefix().tag("target", target.name).send(sender)
    }
}