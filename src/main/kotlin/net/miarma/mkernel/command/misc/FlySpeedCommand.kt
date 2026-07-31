package net.miarma.mkernel.command.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.FloatArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService

@Singleton
class FlySpeedCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) {
    companion object { private const val DEFAULT_SPEED = 0.1f }

    fun register() {
        commandAPICommand(configService.getString("commands.flyspeed.name")) {
            withAliases(*configService.getStringList("commands.flyspeed.aliases").toTypedArray())
            withPermission(configService.getString("commands.flyspeed.permission"))
            withShortDescription(configService.getString("commands.flyspeed.description"))
            withOptionalArguments(FloatArgument(configService.getString("arguments.speed"), 1.0f, 10.0f))
            playerExecutor { sender, args ->
                val speedInput = args[0] as? Float
                if (speedInput == null) {
                    sender.flySpeed = DEFAULT_SPEED
                    messageService.builder(configService.getString("commands.flyspeed.messages.reset")).withPrefix().send(sender)
                    return@playerExecutor
                }

                sender.flySpeed = speedInput / 10.0f
                messageService.builder(configService.getString("commands.flyspeed.messages.changed")).withPrefix().tag("speed", speedInput.toString()).send(sender)
            }
        }
    }
}