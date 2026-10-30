package es.killobyte.nucleo.command.impl.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import es.killobyte.nucleo.api.annotation.RequiresModule
import es.killobyte.nucleo.api.common.ICommand
import es.killobyte.nucleo.common.config.ConfigKeys
import es.killobyte.nucleo.common.module.ModuleLoader
import es.killobyte.nucleo.common.service.impl.ConfigService
import es.killobyte.nucleo.common.service.impl.LastPositionService
import es.killobyte.nucleo.common.service.impl.MessageService
import es.killobyte.nucleo.util.CommandUtil.checkModule

@Singleton
@RequiresModule(ConfigKeys.Modules.Teleport.MAIN)
class BackCommand @Inject constructor(
    private val configService: ConfigService,
    private val lastPositionService: LastPositionService,
    private val messageService: MessageService,
    private val moduleLoader: ModuleLoader
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Back.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Teleport.MAIN)
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