package net.miarma.mkernel.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService

@Singleton
class DayCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString("commands.day.name")) {
            withShortDescription(configService.getString("commands.day.description"))
            withFullDescription(configService.getString("commands.day.description"))
            withPermission(configService.getString("commands.day.permission"))
            playerExecutor { sender, _ ->
                sender.world.time = 1000
                messageService.builder(configService.getString("commands.day.messages.daySet"))
                    .withPrefix()
                    .send(sender)
            }
        }
    }
}
