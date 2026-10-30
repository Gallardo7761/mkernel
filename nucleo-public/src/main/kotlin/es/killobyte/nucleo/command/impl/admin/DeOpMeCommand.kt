package es.killobyte.nucleo.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import es.killobyte.nucleo.api.annotation.RequiresModule
import es.killobyte.nucleo.api.common.ICommand
import es.killobyte.nucleo.common.config.ConfigKeys
import es.killobyte.nucleo.common.service.impl.ConfigService
import es.killobyte.nucleo.common.service.impl.MessageService

@Singleton
@RequiresModule(ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.DEOPME)
class DeOpMeCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) : ICommand {
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