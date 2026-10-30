package es.killobyte.nucleo.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.FloatArgument
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
@RequiresModule(ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.FLYSPEED)
class FlySpeedCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val moduleLoader: ModuleLoader
) : ICommand {
    companion object { private const val DEFAULT_SPEED = 0.1f }

    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.FlySpeed.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.FLYSPEED)
            withAliases(*configService.getStringList(ConfigKeys.Commands.FlySpeed.ALIASES).toTypedArray())
            withPermission(configService.getString(ConfigKeys.Commands.FlySpeed.PERM))
            withShortDescription(configService.getString(ConfigKeys.Commands.FlySpeed.DESC))
            withOptionalArguments(FloatArgument(configService.getString(ConfigKeys.Arguments.SPEED), 1.0f, 10.0f))
            playerExecutor { sender, args ->
                val speedInput = args[0] as? Float
                if (speedInput == null) {
                    sender.flySpeed = DEFAULT_SPEED
                    messageService.builder(configService.getString(ConfigKeys.Commands.FlySpeed.MSG_RESET)).withPrefix()
                        .send(sender)
                    return@playerExecutor
                }

                sender.flySpeed = speedInput / 10.0f
                messageService.builder(configService.getString(ConfigKeys.Commands.FlySpeed.MSG_CHANGED)).withPrefix()
                    .tag("speed", speedInput.toString()).send(sender)
            }
        }
    }
}