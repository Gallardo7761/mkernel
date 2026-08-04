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
import org.bukkit.GameMode

@Singleton
@RequiresModule(ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.GMA)
class GmaCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val moduleLoader: ModuleLoader
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Gma.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.GMA)
            withPermission(configService.getString(ConfigKeys.Commands.Gma.PERM_BASE))
            withOptionalArguments(
                PlayerProfileArgument(configService.getString(ConfigKeys.Arguments.PLAYER))
                    .withPermission(configService.getString(ConfigKeys.Commands.Gma.PERM_OTHERS))
            )
            withShortDescription(configService.getString(ConfigKeys.Commands.Gma.DESC))
            withUsage(configService.getString(ConfigKeys.Commands.Gma.USAGE))
            playerExecutor { sender, args ->
                if (args[0] == null) {
                    sender.gameMode = GameMode.ADVENTURE
                    messageService.builder(configService.getString(ConfigKeys.Commands.Gma.MSG_SELF)).withPrefix().send(sender)
                    return@playerExecutor
                }

                val target = PlayerUtil.fromArg(args[0])
                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.PLAYER_NOT_FOUND)).withPrefix().send(sender)
                    return@playerExecutor
                }

                target.gameMode = GameMode.ADVENTURE
                messageService.builder(configService.getString(ConfigKeys.Commands.Gma.MSG_OTHERS))
                    .withPrefix().tag("player", target.name).send(sender)
            }
        }
    }
}