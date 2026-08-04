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

@Singleton
class SendCoordsCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.SendCoords.NAME)) {
            withArguments(PlayerProfileArgument(configService.getString(ConfigKeys.Arguments.PLAYER)))
            withFullDescription(configService.getString(ConfigKeys.Commands.SendCoords.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.SendCoords.PERM))
            withUsage(configService.getString(ConfigKeys.Commands.SendCoords.USAGE))
            playerExecutor { sender, args ->
                val target = PlayerUtil.fromArg(args[0]) ?: run {
                    messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.PLAYER_NOT_FOUND)).withPrefix().send(sender)
                    return@playerExecutor
                }

                val loc = sender.location
                messageService.builder(configService.getString(ConfigKeys.Commands.SendCoords.MSG_SENT)).withPrefix().tag("target", target.name).send(sender)
                messageService.builder(configService.getString(ConfigKeys.Commands.SendCoords.MSG_COORDS))
                    .withPrefix().forPlayer(target).tag("sender", sender.name)
                    .tag("x", loc.blockX.toString()).tag("y", loc.blockY.toString()).tag("z", loc.blockZ.toString())
                    .send(target)
            }
        }
    }
}