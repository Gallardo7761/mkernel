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
@RequiresModule(ConfigKeys.Modules.World.MAIN, ConfigKeys.Modules.World.TIME_WEATHER_CONTROL)
class DayCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val moduleLoader: ModuleLoader
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Day.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.World.MAIN, ConfigKeys.Modules.World.TIME_WEATHER_CONTROL)
            withShortDescription(configService.getString(ConfigKeys.Commands.Day.DESC))
            withFullDescription(configService.getString(ConfigKeys.Commands.Day.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.Day.PERM))
            playerExecutor { sender, _ ->
                sender.world.time = 1000
                messageService.builder(configService.getString(ConfigKeys.Commands.Day.MSG_SET))
                    .withPrefix()
                    .send(sender)
            }
        }
    }
}