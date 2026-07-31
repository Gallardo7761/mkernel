package net.miarma.mkernel.command.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.LastPositionService
import net.miarma.mkernel.common.service.impl.MessageService

@Singleton
class BackCommand @Inject constructor(
    private val configService: ConfigService,
    private val lastPositionService: LastPositionService,
    private val messageService: MessageService
) {
    fun register() {
        commandAPICommand(configService.getString("commands.back.name")) {
            withPermission(configService.getString("commands.back.permission"))
            withFullDescription(configService.getString("commands.back.description"))
            playerExecutor { sender, _ ->
                val lastLocation = lastPositionService.getLastPosition(sender) ?: run {
                    messageService.builder(configService.getString("language.errors.noLastPosition")).withPrefix().send(sender)
                    return@playerExecutor
                }
                sender.teleportAsync(lastLocation).thenAccept { success ->
                    if (success) {
                        messageService.builder(configService.getString("commands.back.messages.success")).withPrefix().send(sender)
                    }
                }
            }
        }
    }
}