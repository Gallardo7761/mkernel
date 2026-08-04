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
class OpMeCommand @Inject constructor(private val configService: ConfigService, private val messageService: MessageService) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.OpMe.NAME)) {
            withFullDescription(configService.getString(ConfigKeys.Commands.OpMe.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.OpMe.PERM))
            playerExecutor { sender, _ ->
                if (!sender.isOp) {
                    sender.isOp = true
                    messageService.builder(configService.getString(ConfigKeys.Commands.OpMe.MSG_OPPED)).withPrefix().send(sender)
                } else {
                    messageService.builder(configService.getString(ConfigKeys.Commands.OpMe.MSG_ALREADY)).withPrefix().send(sender)
                }
            }
        }
    }
}