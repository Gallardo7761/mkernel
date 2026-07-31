package net.miarma.mkernel.command.impl.tp

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.service.impl.TeleportService
import net.miarma.mkernel.common.teleport.TpaRequest
import net.miarma.mkernel.common.teleport.TpaType
import net.miarma.mkernel.util.PlayerUtil

@Singleton
class TpaAcceptCommand @Inject constructor(
    private val plugin: MKernel,
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val teleportService: TeleportService
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString("commands.tpaccept.name")) {
            withOptionalArguments(PlayerProfileArgument(configService.getString("arguments.player")))
            withPermission(configService.getString("commands.tpaccept.permission"))
            withFullDescription(configService.getString("commands.tpaccept.description"))
            withUsage(configService.getString("commands.tpaccept.usage"))
            playerExecutor { sender, args ->
                plugin.launchSync {
                    if (args[0] == null) {
                        val request = teleportService.getIncomingTpaRequest(sender)
                        if (request == null) {
                            messageService.builder(configService.getString("language.errors.noRequestFound")).withPrefix().send(sender)
                            return@launchSync
                        }
                        acceptRequest(sender, request.from, request)
                    } else {
                        val target = PlayerUtil.fromArg(args[0])
                        if (target == null || !target.isOnline) {
                            messageService.builder(configService.getString("language.errors.playerNotFound")).withPrefix().send(sender)
                            return@launchSync
                        }

                        val request = teleportService.getTpaRequest(target, sender)
                        if (request == null) {
                            messageService.builder(configService.getString("language.errors.noRequestFound")).withPrefix().send(sender)
                            return@launchSync
                        }
                        acceptRequest(sender, target, request)
                    }
                }
            }
        }
    }

    private fun acceptRequest(sender: org.bukkit.entity.Player, target: org.bukkit.entity.Player, request: TpaRequest) {
        teleportService.removeRequest(request)

        if (request.type == TpaType.TPA) {
            target.teleportAsync(sender.location)
        } else if (request.type == TpaType.TPA_HERE) {
            sender.teleportAsync(target.location)
        }

        messageService.builder(configService.getString("commands.tpaccept.messages.acceptedToTarget")).withPrefix().forPlayer(target).tag("sender", sender.name).send(target)
        messageService.builder(configService.getString("commands.tpaccept.messages.accepted")).withPrefix().tag("target", target.name).send(sender)
    }
}