package net.miarma.mkernel.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.annotation.RequiresModule
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService

@Singleton
@RequiresModule(ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.DEOPME)
class DeOpMeCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.DeOpMe.NAME)) {
            withFullDescription(configService.getString(ConfigKeys.Commands.DeOpMe.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.DeOpMe.PERM))
            playerExecutor { sender, _ ->
                if (sender.isOp) {
                    sender.isOp = false
                    messageService.builder(configService.getString(ConfigKeys.Commands.DeOpMe.MSG_DEOPPED)).withPrefix().send(sender)
                } else {
                    messageService.builder(configService.getString(ConfigKeys.Commands.DeOpMe.MSG_NOT_OP)).withPrefix().send(sender)
                }
            }
        }
    }
}