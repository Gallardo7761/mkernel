package es.killobyte.nucleo.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import es.killobyte.nucleo.api.annotation.RequiresModule
import es.killobyte.nucleo.api.common.ICommand
import es.killobyte.nucleo.common.config.ConfigKeys
import es.killobyte.nucleo.common.module.ModuleLoader
import es.killobyte.nucleo.common.service.impl.ConfigService
import es.killobyte.nucleo.common.service.impl.MessageService
import es.killobyte.nucleo.common.service.impl.PlayerService
import es.killobyte.nucleo.util.CommandUtil.checkModule
import es.killobyte.nucleo.util.PlayerUtil

@Singleton
@RequiresModule(ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.FREEZE)
class FreezeCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val playerService: PlayerService,
    private val moduleLoader: ModuleLoader
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Freeze.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Player.MAIN)
            withArguments(PlayerProfileArgument(configService.getString(ConfigKeys.Arguments.PLAYER)))
            withFullDescription(configService.getString(ConfigKeys.Commands.Freeze.DESC))
            withUsage(configService.getString(ConfigKeys.Commands.Freeze.USAGE))
            withPermission(configService.getString(ConfigKeys.Commands.Freeze.PERM))
            playerExecutor { sender, args ->
                val target = PlayerUtil.fromArg(args[0]) ?: run {
                    messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.PLAYER_NOT_FOUND)).withPrefix().send(sender)
                    return@playerExecutor
                }

                if (target == sender) {
                    messageService.builder(configService.getString(ConfigKeys.Messages.Admin.Errors.CANNOT_FREEZE_SELF)).withPrefix().send(sender)
                    return@playerExecutor
                }

                if (playerService.isFrozen(target)) {
                    playerService.setFrozen(target, false)
                    messageService.builder(configService.getString(ConfigKeys.Commands.Freeze.MSG_UNFROZEN)).withPrefix().tag("player", target.name).send(sender)
                    messageService.builder(configService.getString(ConfigKeys.Commands.Freeze.MSG_BEEN_UNFROZEN)).withPrefix().forPlayer(target).tag("sender", sender.name).send(target)
                } else {
                    playerService.setFrozen(target, true)
                    messageService.builder(configService.getString(ConfigKeys.Commands.Freeze.MSG_FROZEN)).withPrefix().tag("player", target.name).send(sender)
                    messageService.builder(configService.getString(ConfigKeys.Commands.Freeze.MSG_BEEN_FROZEN)).withPrefix().forPlayer(target).tag("sender", sender.name).send(target)
                }
            }
        }
    }
}