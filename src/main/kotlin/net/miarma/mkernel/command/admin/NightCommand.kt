package net.miarma.mkernel.command.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService

@Singleton
class NightCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) {
    fun register() {
        commandAPICommand(configService.getString("commands.night.name")) {
            withShortDescription(configService.getString("commands.night.description"))
            withFullDescription(configService.getString("commands.night.description"))
            withPermission(configService.getString("commands.night.permission"))
            playerExecutor { sender, _ ->
                sender.world.time = 13000
                messageService.builder(configService.getString("commands.night.messages.nightSet"))
                    .withPrefix()
                    .send(sender)
            }
        }
    }
}