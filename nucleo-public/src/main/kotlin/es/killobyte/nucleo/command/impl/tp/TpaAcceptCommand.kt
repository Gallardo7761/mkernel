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
import es.killobyte.nucleo.common.teleport.TpaRequest
import es.killobyte.nucleo.common.teleport.TpaType
import es.killobyte.nucleo.util.CommandUtil.checkModule
import es.killobyte.nucleo.util.PlayerUtil

@Singleton
@RequiresModule(ConfigKeys.Modules.Teleport.MAIN)
class TpaAcceptCommand @Inject constructor(
    private val plugin: NucleoPlugin,
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val teleportService: TeleportService,
    private val moduleLoader: ModuleLoader
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.TpAccept.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Teleport.MAIN)
            withOptionalArguments(PlayerProfileArgument(configService.getString(ConfigKeys.Arguments.PLAYER)))
            withPermission(configService.getString(ConfigKeys.Commands.TpAccept.PERM))
            withFullDescription(configService.getString(ConfigKeys.Commands.TpAccept.DESC))
            withUsage(configService.getString(ConfigKeys.Commands.TpAccept.USAGE))
            playerExecutor { sender, args ->
                plugin.launchSync {
                    if (args[0] == null) {
                        val request = teleportService.getIncomingTpaRequest(sender)
                        if (request == null) {
                            messageService.builder(configService.getString(ConfigKeys.Messages.Teleport.Errors.NO_REQ_FOUND)).withPrefix().send(sender)
                            return@launchSync
                        }
                        acceptRequest(sender, request.from, request)
                    } else {
                        val target = PlayerUtil.fromArg(args[0])
                        if (target == null || !target.isOnline) {
                            messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.PLAYER_NOT_FOUND)).withPrefix().send(sender)
                            return@launchSync
                        }

                        val request = teleportService.getTpaRequest(target, sender)
                        if (request == null) {
                            messageService.builder(configService.getString(ConfigKeys.Messages.Teleport.Errors.NO_REQ_FOUND)).withPrefix().send(sender)
                            return@launchSync
                        }
                        acceptRequest(sender, target, request)
                    }
                }
            }
        }
    }

    private fun acceptRequest(sender: org.bukkit.entity.Player, target: org.bukkit.entity.Player, request: TpaRequest) {
        teleportService.removeRequest(request)

        if (request.type == TpaType.TPA) {
            target.teleportAsync(sender.location)
        } else if (request.type == TpaType.TPA_HERE) {
            sender.teleportAsync(target.location)
        }

        messageService.builder(configService.getString(ConfigKeys.Commands.TpAccept.MSG_ACCEPTED_TARGET)).withPrefix().forPlayer(target).tag("sender", sender.name).send(target)
        messageService.builder(configService.getString(ConfigKeys.Commands.TpAccept.MSG_ACCEPTED)).withPrefix().tag("target", target.name).send(sender)
    }
}