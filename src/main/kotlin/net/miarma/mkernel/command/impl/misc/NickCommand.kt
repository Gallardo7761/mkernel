package net.miarma.mkernel.command.impl.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.annotation.RequiresModule
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.service.impl.PlayerService
import net.miarma.mkernel.util.CommandUtil.checkModule
import net.miarma.mkernel.util.PlayerUtil

@Singleton
@RequiresModule(ConfigKeys.Modules.Core.MAIN, ConfigKeys.Modules.Core.Commands.NICK)
class NickCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val playerService: PlayerService,
    private val moduleLoader: ModuleLoader
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Nick.NAME)) {
            checkModule(moduleLoader, configService, messageService, ConfigKeys.Modules.Core.MAIN, ConfigKeys.Modules.Core.Commands.NICK)
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