package net.miarma.mkernel.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.PlayerUtil
import org.bukkit.GameMode

@Singleton
class GmsCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Gms.NAME)) {
            withPermission(configService.getString(ConfigKeys.Commands.Gms.PERM_BASE))
            withOptionalArguments(
                PlayerProfileArgument(configService.getString(ConfigKeys.Arguments.PLAYER))
                    .withPermission(configService.getString(ConfigKeys.Commands.Gms.PERM_OTHERS))
            )
            withShortDescription(configService.getString(ConfigKeys.Commands.Gms.DESC))
            withUsage(configService.getString(ConfigKeys.Commands.Gms.USAGE))
            playerExecutor { sender, args ->
                if (args[0] == null) {
                    sender.gameMode = GameMode.SURVIVAL
                    messageService.builder(configService.getString(ConfigKeys.Commands.Gms.MSG_SELF)).withPrefix().send(sender)
                    return@playerExecutor
                }

                val target = PlayerUtil.fromArg(args[0])
                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.PLAYER_NOT_FOUND)).withPrefix().send(sender)
                    return@playerExecutor
                }

                target.gameMode = GameMode.SURVIVAL
                messageService.builder(configService.getString(ConfigKeys.Commands.Gms.MSG_OTHERS))
                    .withPrefix().tag("player", target.name).send(sender)
            }
        }
    }
}