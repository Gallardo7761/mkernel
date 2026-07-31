package net.miarma.mkernel.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService

@Singleton
class RainCommand @Inject constructor(private val configService: ConfigService, private val messageService: MessageService) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString("commands.rain.name")) {
            withShortDescription(configService.getString("commands.rain.description"))
            withFullDescription(configService.getString("commands.rain.description"))
            withPermission(configService.getString("commands.rain.permission"))
            playerExecutor { sender, _ ->
                sender.world.setStorm(true)
                sender.world.isThundering = false
                messageService.builder(configService.getString("commands.rain.messages.rainSet")).withPrefix().send(sender)
            }
        }
    }
}