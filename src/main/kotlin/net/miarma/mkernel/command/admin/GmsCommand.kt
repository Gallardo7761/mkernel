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
class GmsCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) {
    fun register() {
        commandAPICommand(configService.getString("commands.gms.name")) {
            withPermission(configService.getString("commands.gms.permissions.base"))
            withOptionalArguments(
                PlayerProfileArgument(configService.getString("arguments.player"))
                    .withPermission(configService.getString("commands.gms.permissions.others"))
            )
            withShortDescription(configService.getString("commands.gms.description"))
            withUsage(configService.getString("commands.gms.usage"))
            playerExecutor { sender, args ->
                if (args[0] == null) {
                    sender.gameMode = GameMode.SURVIVAL
                    messageService.builder(configService.getString("commands.gms.messages.self")).withPrefix().send(sender)
                    return@playerExecutor
                }

                val target = PlayerUtil.fromArg(args[0])
                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString("language.errors.playerNotFound")).withPrefix().send(sender)
                    return@playerExecutor
                }

                target.gameMode = GameMode.SURVIVAL
                messageService.builder(configService.getString("commands.gms.messages.others"))
                    .withPrefix().tag("player", target.name).send(sender)
            }
        }
    }
}