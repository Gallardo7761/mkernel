package net.miarma.mkernel.command.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.PlayerUtil
import org.bukkit.GameMode

@Singleton
class GmaCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) {
    fun register() {
        commandAPICommand(configService.getString("commands.gma.name")) {
            withPermission(configService.getString("commands.gma.permissions.base"))
            withOptionalArguments(
                PlayerProfileArgument(configService.getString("arguments.player"))
                    .withPermission(configService.getString("commands.gma.permissions.others"))
            )
            withShortDescription(configService.getString("commands.gma.description"))
            withUsage(configService.getString("commands.gma.usage"))
            playerExecutor { sender, args ->
                if (args[0] == null) {
                    sender.gameMode = GameMode.ADVENTURE
                    messageService.builder(configService.getString("commands.gma.messages.self")).withPrefix().send(sender)
                    return@playerExecutor
                }

                val target = PlayerUtil.fromArg(args[0])
                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString("language.errors.playerNotFound")).withPrefix().send(sender)
                    return@playerExecutor
                }

                target.gameMode = GameMode.ADVENTURE
                messageService.builder(configService.getString("commands.gma.messages.others"))
                    .withPrefix().tag("player", target.name).send(sender)
            }
        }
    }
}