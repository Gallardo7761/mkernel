package net.miarma.mkernel.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService

@Singleton
class ThunderCommand @Inject constructor(private val configService: ConfigService, private val messageService: MessageService) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString("commands.thunder.name")) {
            withShortDescription(configService.getString("commands.thunder.description"))
            withFullDescription(configService.getString("commands.thunder.description"))
            withPermission(configService.getString("commands.thunder.permission"))
            playerExecutor { sender, _ ->
                sender.world.setStorm(true)
                sender.world.isThundering = true
                messageService.builder(configService.getString("commands.thunder.messages.thunderSet")).withPrefix().send(sender)
            }
        }
    }
}