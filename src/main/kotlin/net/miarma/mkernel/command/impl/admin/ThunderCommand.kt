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
class ThunderCommand @Inject constructor(private val configService: ConfigService, private val messageService: MessageService) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Thunder.NAME)) {
            withShortDescription(configService.getString(ConfigKeys.Commands.Thunder.DESC))
            withFullDescription(configService.getString(ConfigKeys.Commands.Thunder.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.Thunder.PERM))
            playerExecutor { sender, _ ->
                sender.world.setStorm(true)
                sender.world.isThundering = true
                messageService.builder(configService.getString(ConfigKeys.Commands.Thunder.MSG_SET)).withPrefix().send(sender)
            }
        }
    }
}