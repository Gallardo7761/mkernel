package net.miarma.mkernel.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService

@Singleton
class NightCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Night.NAME)) {
            withShortDescription(configService.getString(ConfigKeys.Commands.Night.DESC))
            withFullDescription(configService.getString(ConfigKeys.Commands.Night.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.Night.PERM))
            playerExecutor { sender, _ ->
                sender.world.time = 13000
                messageService.builder(configService.getString(ConfigKeys.Commands.Night.MSG_SET))
                    .withPrefix()
                    .send(sender)
            }
        }
    }
}