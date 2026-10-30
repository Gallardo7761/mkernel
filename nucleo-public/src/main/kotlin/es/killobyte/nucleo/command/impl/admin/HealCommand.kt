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
import es.killobyte.nucleo.util.CommandUtil.checkModule
import es.killobyte.nucleo.util.PlayerUtil

@Singleton
@RequiresModule(ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.HEAL)
class HealCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val moduleLoader: ModuleLoader
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Heal.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.HEAL)
            withPermission(configService.getString(ConfigKeys.Commands.Heal.PERM_BASE))
            withOptionalArguments(
                PlayerProfileArgument(configService.getString(ConfigKeys.Arguments.PLAYER))
                    .withPermission(configService.getString(ConfigKeys.Commands.Heal.PERM_OTHERS))
            )
            withFullDescription(configService.getString(ConfigKeys.Commands.Heal.DESC))
            withUsage(configService.getString(ConfigKeys.Commands.Heal.USAGE))
            playerExecutor { sender, args ->
                if (args[0] == null) {
                    sender.foodLevel = 20
                    sender.health = 20.0
                    sender.fireTicks = 0
                    messageService.builder(configService.getString(ConfigKeys.Commands.Heal.MSG_HEALED_SELF)).withPrefix().send(sender)
                    return@playerExecutor
                }

                val target = PlayerUtil.fromArg(args[0])
                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.PLAYER_NOT_FOUND)).withPrefix().send(sender)
                    return@playerExecutor
                }

                target.foodLevel = 20
                target.health = 20.0
                target.fireTicks = 0

                messageService.builder(configService.getString(ConfigKeys.Commands.Heal.MSG_HEALED_PLAYER)).withPrefix().tag("player", target.name).send(sender)
                messageService.builder(configService.getString(ConfigKeys.Commands.Heal.MSG_BEEN_HEALED)).withPrefix().forPlayer(target).tag("sender", sender.name).send(target)
            }
        }
    }
}