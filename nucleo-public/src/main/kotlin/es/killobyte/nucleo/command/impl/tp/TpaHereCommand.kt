package es.killobyte.nucleo.command.impl.tp

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.annotation.RequiresModule
import es.killobyte.nucleo.api.common.ICommand
import es.killobyte.nucleo.common.config.ConfigKeys
import es.killobyte.nucleo.common.module.ModuleLoader
import es.killobyte.nucleo.common.service.impl.ConfigService
import es.killobyte.nucleo.common.service.impl.MessageService
import es.killobyte.nucleo.common.service.impl.TeleportService
import es.killobyte.nucleo.common.teleport.TpaType
import es.killobyte.nucleo.util.CommandUtil.checkModule
import es.killobyte.nucleo.util.PlayerUtil

@Singleton
@RequiresModule(ConfigKeys.Modules.Teleport.MAIN)
class TpaHereCommand @Inject constructor(
    private val plugin: NucleoPlugin,
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val teleportService: TeleportService,
    private val moduleLoader: ModuleLoader
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.TpaHere.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Teleport.MAIN)
            withArguments(PlayerProfileArgument(configService.getString(ConfigKeys.Arguments.PLAYER)))
            withPermission(configService.getString(ConfigKeys.Commands.TpaHere.PERM))
            withFullDescription(configService.getString(ConfigKeys.Commands.TpaHere.DESC))
            withUsage(configService.getString(ConfigKeys.Commands.TpaHere.USAGE))
            playerExecutor { sender, args ->
                val target = PlayerUtil.fromArg(args[0])
                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.PLAYER_NOT_FOUND)).withPrefix().send(sender)
                    return@playerExecutor
                }

                if (target == sender) {
                    messageService.builder(configService.getString(ConfigKeys.Messages.Teleport.Errors.CANT_TP_SELF)).withPrefix().send(sender)
                    return@playerExecutor
                }

                plugin.launchSync {
                    val request = teleportService.getTpaRequest(sender, target)
                    if (request != null) {
                        messageService.builder(configService.getString(ConfigKeys.Messages.Teleport.Errors.REQ_ALREADY_SENT)).withPrefix().send(sender)
                    } else {
                        teleportService.addRequest(sender, target, TpaType.TPA_HERE)
                        messageService.builder(configService.getString(ConfigKeys.Commands.TpaHere.MSG_TO)).withPrefix().tag("target", target.name).send(sender)
                        messageService.builder(configService.getString(ConfigKeys.Commands.TpaHere.MSG_FROM)).withPrefix().forPlayer(target).tag("sender", sender.name).send(target)
                    }
                }
            }
        }
    }
}