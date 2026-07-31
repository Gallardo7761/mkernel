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

@Singleton
class SendCoordsCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString("commands.sendcoords.name")) {
            withArguments(PlayerProfileArgument(configService.getString("arguments.player")))
            withFullDescription(configService.getString("commands.sendcoords.description"))
            withPermission(configService.getString("commands.sendcoords.permission"))
            withUsage(configService.getString("commands.sendcoords.usage"))
            playerExecutor { sender, args ->
                val target = PlayerUtil.fromArg(args[0]) ?: run {
                    messageService.builder(configService.getString("language.errors.playerNotFound")).withPrefix().send(sender)
                    return@playerExecutor
                }

                val loc = sender.location
                messageService.builder(configService.getString("commands.sendcoords.messages.sent")).withPrefix().tag("target", target.name).send(sender)
                messageService.builder(configService.getString("commands.sendcoords.messages.coordsMsg"))
                    .withPrefix().forPlayer(target).tag("sender", sender.name)
                    .tag("x", loc.blockX.toString()).tag("y", loc.blockY.toString()).tag("z", loc.blockZ.toString())
                    .send(target)
            }
        }
    }
}