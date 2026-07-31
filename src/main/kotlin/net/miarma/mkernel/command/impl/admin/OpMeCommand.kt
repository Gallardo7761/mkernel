package net.miarma.mkernel.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService

@Singleton
class OpMeCommand @Inject constructor(private val configService: ConfigService, private val messageService: MessageService) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString("commands.opme.name")) {
            withFullDescription(configService.getString("commands.opme.description"))
            withPermission(configService.getString("commands.opme.permission"))
            playerExecutor { sender, _ ->
                if (!sender.isOp) {
                    sender.isOp = true
                    messageService.builder(configService.getString("commands.opme.messages.opped")).withPrefix().send(sender)
                } else {
                    messageService.builder(configService.getString("commands.opme.messages.alreadyOp")).withPrefix().send(sender)
                }
            }
        }
    }
}