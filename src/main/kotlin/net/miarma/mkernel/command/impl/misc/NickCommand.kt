package net.miarma.mkernel.command.impl.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.service.impl.PlayerService
import net.miarma.mkernel.util.PlayerUtil

@Singleton
class NickCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val playerService: PlayerService
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString("commands.nick.name")) {
            withPermission(configService.getString("commands.nick.permissions.base"))
            withOptionalArguments(
                StringArgument(configService.getString("arguments.nickname")),
                PlayerProfileArgument(configService.getString("arguments.player"))
                    .withPermission(configService.getString("commands.nick.permissions.others"))
            )
            withFullDescription(configService.getString("commands.nick.description"))
            playerExecutor { sender, args ->
                val nick = args[0] as? String
                val targetArg = if (args.count() > 1) args[1] else null

                val target = if (targetArg != null) PlayerUtil.fromArg(targetArg) else sender

                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString("language.errors.playerNotFound"))
                        .withPrefix().send(sender)
                    return@playerExecutor
                }

                if (nick.isNullOrEmpty() || nick.equals("off", ignoreCase = true)) {
                    playerService.setNick(target, null)

                    if (target == sender) {
                        messageService.builder(configService.getString("commands.nick.messages.reset"))
                            .withPrefix().send(sender)
                    } else {
                        messageService.builder(configService.getString("commands.nick.messages.resetOthers"))
                            .withPrefix().tag("target", target.name).send(sender)
                    }
                } else {
                    val success = playerService.setNick(target, nick)

                    if (!success) {
                        messageService.builder(configService.getString("language.errors.nickBlacklisted"))
                            .withPrefix().send(sender)
                        return@playerExecutor
                    }

                    if (target == sender) {
                        messageService.builder(configService.getString("commands.nick.messages.set"))
                            .withPrefix().tag("nickname", nick).send(sender)
                    } else {
                        messageService.builder(configService.getString("commands.nick.messages.setOthers"))
                            .withPrefix().tag("target", target.name).tag("nickname", nick).send(sender)
                    }
                }
            }
        }
    }
}