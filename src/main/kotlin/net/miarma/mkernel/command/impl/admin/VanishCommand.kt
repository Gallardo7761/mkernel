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
class VanishCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val playerService: PlayerService
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString("commands.vanish.name")) {
            withAliases("v")
            withFullDescription(configService.getString("commands.vanish.description"))
            withPermission(configService.getString("commands.vanish.permission"))
            playerExecutor { sender, _ ->
                if (playerService.isVanished(sender)) {
                    playerService.setVanished(sender, false)
                    sender.isInvisible = false
                    sender.canPickupItems = true
                    messageService.builder(configService.getString("commands.vanish.messages.unvanished")).withPrefix().send(sender)
                } else {
                    playerService.setVanished(sender, true)
                    sender.isInvisible = true
                    sender.canPickupItems = false
                    messageService.builder(configService.getString("commands.vanish.messages.vanished")).withPrefix().send(sender)
                }
            }
        }
    }
}