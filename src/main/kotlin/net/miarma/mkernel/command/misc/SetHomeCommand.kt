package net.miarma.mkernel.command.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.DatabaseService
import net.miarma.mkernel.common.service.impl.MessageService

@Singleton
class SetHomeCommand @Inject constructor(
    private val configService: ConfigService,
    private val databaseService: DatabaseService,
    private val messageService: MessageService
) {
    fun register() {
        commandAPICommand(configService.getString("commands.sethome.name")) {
            withPermission(configService.getString("commands.sethome.permission"))
            withFullDescription(configService.getString("commands.sethome.description"))
            playerExecutor { sender, _ ->
                val loc = sender.location
                databaseService.setHome(sender, loc)
                messageService.builder(configService.getString("commands.sethome.messages.homeSet"))
                    .withPrefix()
                    .tag("x", loc.blockX.toString())
                    .tag("y", loc.blockY.toString())
                    .tag("z", loc.blockZ.toString())
                    .send(sender)
            }
        }
    }
}