package net.miarma.mkernel.command.impl.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.LastPositionService
import net.miarma.mkernel.common.service.impl.MessageService

@Singleton
class BackCommand @Inject constructor(
    private val configService: ConfigService,
    private val lastPositionService: LastPositionService,
    private val messageService: MessageService
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Back.NAME)) {
            withPermission(configService.getString(ConfigKeys.Commands.Back.PERM))
            withFullDescription(configService.getString(ConfigKeys.Commands.Back.DESC))
            playerExecutor { sender, _ ->
                val lastLocation = lastPositionService.getLastPosition(sender) ?: run {
                    messageService.builder(configService.getString(ConfigKeys.Messages.Teleport.Errors.NO_LAST_POS)).withPrefix().send(sender)
                    return@playerExecutor
                }
                sender.teleportAsync(lastLocation).thenAccept { success ->
                    if (success) {
                        messageService.builder(configService.getString(ConfigKeys.Commands.Back.MSG_SUCCESS)).withPrefix().send(sender)
                    }
                }
            }
        }
    }
}