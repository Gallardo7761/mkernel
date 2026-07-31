package net.miarma.mkernel.command.impl.roleplay

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import org.bukkit.Bukkit

@Singleton
class DoCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString("commands.do.name")) {
            withArguments(GreedyStringArgument(configService.getString("arguments.message")))
            withFullDescription(configService.getString("commands.do.description"))
            withPermission(configService.getString("commands.do.permission"))
            playerExecutor { sender, args ->
                val message = args[0] as String
                val builder = messageService.builder("<blue>(<player>) [Do] <gray><message>")
                    .tag("player", sender.name)
                    .tag("message", message)

                Bukkit.getServer().onlinePlayers
                    .filter { p -> p.world == sender.world && p.location.distance(sender.location) < 25 }
                    .forEach { p -> builder.send(p) }
            }
        }
    }
}