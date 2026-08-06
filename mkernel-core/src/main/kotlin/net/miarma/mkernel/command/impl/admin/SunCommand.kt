package net.miarma.mkernel.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.api.annotation.RequiresModule
import net.miarma.mkernel.api.common.ICommand
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.CommandUtil.checkModule

@Singleton
@RequiresModule(ConfigKeys.Modules.World.MAIN, ConfigKeys.Modules.World.TIME_WEATHER_CONTROL)
class SunCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val moduleLoader: ModuleLoader
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Sun.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.World.MAIN, ConfigKeys.Modules.World.TIME_WEATHER_CONTROL)
            withShortDescription(configService.getString(ConfigKeys.Commands.Sun.DESC))
            withFullDescription(configService.getString(ConfigKeys.Commands.Sun.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.Sun.PERM))
            playerExecutor { sender, _ ->
                sender.world.setStorm(false)
                sender.world.isThundering = false
                messageService.builder(configService.getString(ConfigKeys.Commands.Sun.MSG_SET)).withPrefix().send(sender)
            }
        }
    }
}