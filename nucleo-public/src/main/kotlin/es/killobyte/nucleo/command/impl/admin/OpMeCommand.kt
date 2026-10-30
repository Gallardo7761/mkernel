package es.killobyte.nucleo.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import es.killobyte.nucleo.api.annotation.RequiresModule
import es.killobyte.nucleo.api.common.ICommand
import es.killobyte.nucleo.common.config.ConfigKeys
import es.killobyte.nucleo.common.module.ModuleLoader
import es.killobyte.nucleo.common.service.impl.ConfigService
import es.killobyte.nucleo.common.service.impl.MessageService
import es.killobyte.nucleo.util.CommandUtil.checkModule

@Singleton
@RequiresModule(ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.OPME)
class OpMeCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val moduleLoader: ModuleLoader
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.OpMe.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.OPME)
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