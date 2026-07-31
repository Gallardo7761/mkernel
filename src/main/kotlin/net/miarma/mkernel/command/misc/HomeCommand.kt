package net.miarma.mkernel.command.misc

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.DatabaseService
import net.miarma.mkernel.common.service.impl.MessageService
import org.bukkit.Bukkit

@Singleton
class HomeCommand @Inject constructor(
    private val plugin: MKernel,
    private val configService: ConfigService,
    private val databaseService: DatabaseService,
    private val messageService: MessageService
) {
    fun register() {
        commandAPICommand(configService.getString("commands.home.name")) {
            withPermission(configService.getString("commands.home.permission"))
            withFullDescription(configService.getString("commands.home.description"))
            playerExecutor { sender, _ ->
                plugin.launchAsync {
                    val loc = databaseService.getHome(sender)

                    Bukkit.getScheduler().runTask(plugin, Runnable {
                        if (loc != null) {
                            sender.teleportAsync(loc).thenAccept { success ->
                                if (success) {
                                    messageService.builder(configService.getString("commands.home.messages.teleported"))
                                        .withPrefix().send(sender)
                                }
                            }
                        } else {
                            messageService.builder(configService.getString("commands.home.messages.homeDoesNotExist"))
                                .withPrefix().send(sender)
                        }
                    })
                }
            }
        }
    }
}