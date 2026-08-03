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
class RainCommand @Inject constructor(private val configService: ConfigService, private val messageService: MessageService) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Rain.NAME)) {
            withShortDescription(configService.getString(ConfigKeys.Commands.Rain.DESC))
            withFullDescription(configService.getString(ConfigKeys.Commands.Rain.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.Rain.PERM))
            playerExecutor { sender, _ ->
                sender.world.setStorm(true)
                sender.world.isThundering = false
                messageService.builder(configService.getString(ConfigKeys.Commands.Rain.MSG_SET)).withPrefix().send(sender)
            }
        }
    }
}