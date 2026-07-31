package net.miarma.mkernel.command.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService

@Singleton
class SunCommand @Inject constructor(private val configService: ConfigService, private val messageService: MessageService) {
    fun register() {
        commandAPICommand(configService.getString("commands.sun.name")) {
            withShortDescription(configService.getString("commands.sun.description"))
            withFullDescription(configService.getString("commands.sun.description"))
            withPermission(configService.getString("commands.sun.permission"))
            playerExecutor { sender, _ ->
                sender.world.setStorm(false)
                sender.world.isThundering = false
                messageService.builder(configService.getString("commands.sun.messages.sunSet")).withPrefix().send(sender)
            }
        }
    }
}