package net.miarma.mkernel.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.config.ConfigKeys
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
        commandAPICommand(configService.getString(ConfigKeys.Commands.Vanish.NAME)) {
            withAliases("v")
            withFullDescription(configService.getString(ConfigKeys.Commands.Vanish.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.Vanish.PERM))
            playerExecutor { sender, _ ->
                if (playerService.isVanished(sender)) {
                    playerService.setVanished(sender, false)
                    sender.isInvisible = false
                    sender.canPickupItems = true
                    messageService.builder(configService.getString(ConfigKeys.Commands.Vanish.MSG_UNVANISHED)).withPrefix().send(sender)
                } else {
                    playerService.setVanished(sender, true)
                    sender.isInvisible = true
                    sender.canPickupItems = false
                    messageService.builder(configService.getString(ConfigKeys.Commands.Vanish.MSG_VANISHED)).withPrefix().send(sender)
                }
            }
        }
    }
}