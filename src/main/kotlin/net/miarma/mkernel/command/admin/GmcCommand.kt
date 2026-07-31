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
class GmcCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) {
    fun register() {
        commandAPICommand(configService.getString("commands.gmc.name")) {
            withPermission(configService.getString("commands.gmc.permissions.base"))
            withOptionalArguments(
                PlayerProfileArgument(configService.getString("arguments.player"))
                    .withPermission(configService.getString("commands.gmc.permissions.others"))
            )
            withShortDescription(configService.getString("commands.gmc.description"))
            withUsage(configService.getString("commands.gmc.usage"))
            playerExecutor { sender, args ->
                if (args[0] == null) {
                    sender.gameMode = GameMode.CREATIVE
                    messageService.builder(configService.getString("commands.gmc.messages.self")).withPrefix().send(sender)
                    return@playerExecutor
                }

                val target = PlayerUtil.fromArg(args[0])
                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString("language.errors.playerNotFound")).withPrefix().send(sender)
                    return@playerExecutor
                }

                target.gameMode = GameMode.CREATIVE
                messageService.builder(configService.getString("commands.gmc.messages.others"))
                    .withPrefix().tag("player", target.name).send(sender)
            }
        }
    }
}