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
import org.bukkit.GameMode

@Singleton
@RequiresModule(ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.GMC)
class GmcCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val moduleLoader: ModuleLoader
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Gmc.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.GMC)
            withPermission(configService.getString(ConfigKeys.Commands.Gmc.PERM_BASE))
            withOptionalArguments(
                PlayerProfileArgument(configService.getString(ConfigKeys.Arguments.PLAYER))
                    .withPermission(configService.getString(ConfigKeys.Commands.Gmc.PERM_OTHERS))
            )
            withShortDescription(configService.getString(ConfigKeys.Commands.Gmc.DESC))
            withUsage(configService.getString(ConfigKeys.Commands.Gmc.USAGE))
            playerExecutor { sender, args ->
                if (args[0] == null) {
                    sender.gameMode = GameMode.CREATIVE
                    messageService.builder(configService.getString(ConfigKeys.Commands.Gmc.MSG_SELF)).withPrefix().send(sender)
                    return@playerExecutor
                }

                val target = PlayerUtil.fromArg(args[0])
                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.PLAYER_NOT_FOUND)).withPrefix().send(sender)
                    return@playerExecutor
                }

                target.gameMode = GameMode.CREATIVE
                messageService.builder(configService.getString(ConfigKeys.Commands.Gmc.MSG_OTHERS))
                    .withPrefix().tag("player", target.name).send(sender)
            }
        }
    }
}