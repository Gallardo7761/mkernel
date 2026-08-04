package net.miarma.mkernel.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.annotation.RequiresModule
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.CommandUtil.checkModule
import net.miarma.mkernel.util.PlayerUtil

@Singleton
@RequiresModule(ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.HEAL)
class HealCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val moduleLoader: ModuleLoader
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Heal.NAME)) {
            checkModule(moduleLoader, configService, messageService, ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.HEAL)
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