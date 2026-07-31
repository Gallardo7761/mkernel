package net.miarma.mkernel.command.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService

@Singleton
class DeOpMeCommand @Inject constructor(private val configService: ConfigService, private val messageService: MessageService) {
    fun register() {
        commandAPICommand(configService.getString("commands.deopme.name")) {
            withFullDescription(configService.getString("commands.deopme.description"))
            withPermission(configService.getString("commands.deopme.permission"))
            playerExecutor { sender, _ ->
                if (sender.isOp) {
                    sender.isOp = false
                    messageService.builder(configService.getString("commands.deopme.messages.deOpped")).withPrefix().send(sender)
                } else {
                    messageService.builder(configService.getString("commands.deopme.messages.youAreNotOp")).withPrefix().send(sender)
                }
            }
        }
    }
}