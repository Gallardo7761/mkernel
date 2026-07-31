package net.miarma.mkernel.command.troll

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.PlayerUtil

@Singleton
class LaunchCommand @Inject constructor(private val configService: ConfigService, private val messageService: MessageService) {
    fun register() {
        commandAPICommand(configService.getString("commands.launch.name")) {
            withArguments(PlayerProfileArgument(configService.getString("arguments.player")))
            withPermission(configService.getString("commands.launch.permission"))
            withFullDescription(configService.getString("commands.launch.description"))
            withUsage(configService.getString("commands.launch.usage"))
            playerExecutor { sender, args ->
                val target = PlayerUtil.fromArg(args[0]) ?: run {
                    messageService.builder(configService.getString("language.errors.playerNotFound")).withPrefix().send(sender)
                    return@playerExecutor
                }

                target.velocity = target.velocity.setY(3.0)
                messageService.builder(configService.getString("commands.launch.messages.launched")).withPrefix().tag("player", target.name).send(sender)
            }
        }
    }
}