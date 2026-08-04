package net.miarma.mkernel.command.impl.misc

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
import java.util.logging.Logger

@Singleton
class LobbyCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val logger: Logger
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Lobby.NAME)) {
            withFullDescription(configService.getString(ConfigKeys.Commands.Lobby.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.Lobby.PERM_BASE))
            withOptionalArguments(
                PlayerProfileArgument(configService.getString(ConfigKeys.Arguments.PLAYER))
                    .withPermission(configService.getString(ConfigKeys.Commands.Lobby.PERM_OTHERS))
            )
            playerExecutor { sender, args ->
                val lobby = configService.getLobbyWorld()
                val lobbyCoords = lobby.toLocation()

                if (lobbyCoords.world == null) {
                    messageService.builder(configService.getString(ConfigKeys.Messages.Teleport.Errors.LOBBY_NOT_EXIST)).withPrefix().send(sender)
                    logger.warning("The lobby defined in config does not exist!")
                    return@playerExecutor
                }

                if (args[0] == null) {
                    sender.teleportAsync(lobbyCoords).thenAccept { success ->
                        if (success) messageService.builder(configService.getString(ConfigKeys.Commands.Lobby.MSG_TELEPORTED)).withPrefix().send(sender)
                    }
                    return@playerExecutor
                }

                val target = PlayerUtil.fromArg(args[0])
                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.PLAYER_NOT_FOUND)).withPrefix().send(sender)
                    return@playerExecutor
                }

                target.teleportAsync(lobbyCoords).thenAccept { success ->
                    if (success) {
                        messageService.builder(configService.getString(ConfigKeys.Commands.Lobby.MSG_YOU_OTHERS)).withPrefix().tag("target", target.name).send(sender)
                        messageService.builder(configService.getString(ConfigKeys.Commands.Lobby.MSG_OTHERS_YOU)).withPrefix().forPlayer(target).tag("sender", sender.name).send(target)
                    }
                }
            }
        }
    }
}