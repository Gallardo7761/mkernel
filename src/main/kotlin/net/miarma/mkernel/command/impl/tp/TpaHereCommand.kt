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
import net.miarma.mkernel.common.teleport.TpaType
import net.miarma.mkernel.util.PlayerUtil

@Singleton
class TpaHereCommand @Inject constructor(
    private val plugin: MKernel,
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val teleportService: TeleportService
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString("commands.tpahere.name")) {
            withArguments(PlayerProfileArgument(configService.getString("arguments.player")))
            withPermission(configService.getString("commands.tpahere.permission"))
            withFullDescription(configService.getString("commands.tpahere.description"))
            withUsage(configService.getString("commands.tpahere.usage"))
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

                plugin.launchSync {
                    val request = teleportService.getTpaRequest(sender, target)
                    if (request != null) {
                        messageService.builder(configService.getString("language.errors.requestAlreadySent")).withPrefix().send(sender)
                    } else {
                        teleportService.addRequest(sender, target, TpaType.TPA_HERE)
                        messageService.builder(configService.getString("commands.tpahere.messages.tpaToPlayer")).withPrefix().tag("target", target.name).send(sender)
                        messageService.builder(configService.getString("commands.tpahere.messages.tpaFromPlayer")).withPrefix().forPlayer(target).tag("sender", sender.name).send(target)
                    }
                }
            }
        }
    }
}