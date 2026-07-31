package net.miarma.mkernel.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.service.impl.PlayerService

@Singleton
class SpyCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val playerService: PlayerService
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString("commands.spy.name")) {
            withFullDescription(configService.getString("commands.spy.description"))
            withPermission(configService.getString("commands.spy.permission"))
            playerExecutor { sender, _ ->
                if (playerService.canSpy(sender)) {
                    playerService.setSpy(sender, false)
                    messageService.builder(configService.getString("commands.spy.messages.disabled")).withPrefix().send(sender)
                } else {
                    playerService.setSpy(sender, true)
                    messageService.builder(configService.getString("commands.spy.messages.enabled")).withPrefix().send(sender)
                }
            }
        }
    }
}