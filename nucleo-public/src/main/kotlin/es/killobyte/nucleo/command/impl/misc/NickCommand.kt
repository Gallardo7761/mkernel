package es.killobyte.nucleo.command.impl.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.arguments.StringArgument
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
@RequiresModule(ConfigKeys.Modules.Core.MAIN, ConfigKeys.Modules.Core.Commands.NICK)
class NickCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val playerService: PlayerService,
    private val moduleLoader: ModuleLoader
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Nick.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Core.MAIN, ConfigKeys.Modules.Core.Commands.NICK)
            withPermission(configService.getString(ConfigKeys.Commands.Nick.PERM_BASE))
            withOptionalArguments(
                StringArgument(configService.getString(ConfigKeys.Arguments.NICKNAME)),
                PlayerProfileArgument(configService.getString(ConfigKeys.Arguments.PLAYER))
                    .withPermission(configService.getString(ConfigKeys.Commands.Nick.PERM_OTHERS))
            )
            withFullDescription(configService.getString(ConfigKeys.Commands.Nick.DESC))
            playerExecutor { sender, args ->
                val nick = args[0] as? String
                val targetArg = if (args.count() > 1) args[1] else null

                val target = if (targetArg != null) PlayerUtil.fromArg(targetArg) else sender

                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.PLAYER_NOT_FOUND))
                        .withPrefix().send(sender)
                    return@playerExecutor
                }

                if (nick.isNullOrEmpty() || nick.equals("off", ignoreCase = true)) {
                    playerService.setNick(target, null)

                    if (target == sender) {
                        messageService.builder(configService.getString(ConfigKeys.Commands.Nick.MSG_RESET))
                            .withPrefix().send(sender)
                    } else {
                        messageService.builder(configService.getString(ConfigKeys.Commands.Nick.MSG_RESET_OTHERS))
                            .withPrefix().tag("target", target.name).send(sender)
                    }
                } else {
                    val success = playerService.setNick(target, nick)

                    if (!success) {
                        messageService.builder(configService.getString(ConfigKeys.Messages.Misc.Errors.NICK_BLACKLISTED))
                            .withPrefix().send(sender)
                        return@playerExecutor
                    }

                    if (target == sender) {
                        messageService.builder(configService.getString(ConfigKeys.Commands.Nick.MSG_SET))
                            .withPrefix().tag("nickname", nick).send(sender)
                    } else {
                        messageService.builder(configService.getString(ConfigKeys.Commands.Nick.MSG_SET_OTHERS))
                            .withPrefix().tag("target", target.name).tag("nickname", nick).send(sender)
                    }
                }
            }
        }
    }
}