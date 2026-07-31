package net.miarma.mkernel.command.impl.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.PlayerUtil
import java.util.logging.Logger

@Singleton
class LobbyCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val logger: Logger
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString("commands.lobby.name")) {
            withFullDescription(configService.getString("commands.lobby.description"))
            withPermission(configService.getString("commands.lobby.permissions.base"))
            withOptionalArguments(
                PlayerProfileArgument(configService.getString("arguments.player"))
                    .withPermission(configService.getString("commands.lobby.permissions.others"))
            )
            playerExecutor { sender, args ->
                val lobby = configService.getLobbyWorld()
                val lobbyCoords = lobby.toLocation()

                if (lobbyCoords.world == null) {
                    messageService.builder(configService.getString("language.errors.lobbyDoesNotExist")).withPrefix().send(sender)
                    logger.warning("The lobby defined in config does not exist!")
                    return@playerExecutor
                }

                if (args[0] == null) {
                    sender.teleportAsync(lobbyCoords).thenAccept { success ->
                        if (success) messageService.builder(configService.getString("commands.lobby.messages.teleported")).withPrefix().send(sender)
                    }
                    return@playerExecutor
                }

                val target = PlayerUtil.fromArg(args[0])
                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString("language.errors.playerNotFound")).withPrefix().send(sender)
                    return@playerExecutor
                }

                target.teleportAsync(lobbyCoords).thenAccept { success ->
                    if (success) {
                        messageService.builder(configService.getString("commands.lobby.messages.lobbyYouOthers")).withPrefix().tag("target", target.name).send(sender)
                        messageService.builder(configService.getString("commands.lobby.messages.lobbyOthersYou")).withPrefix().forPlayer(target).tag("sender", sender.name).send(target)
                    }
                }
            }
        }
    }
}