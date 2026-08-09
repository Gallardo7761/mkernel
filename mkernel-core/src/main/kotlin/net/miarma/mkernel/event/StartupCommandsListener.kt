package net.miarma.mkernel.event

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.service.impl.ConfigService
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent

@Singleton
class StartupCommandsListener @Inject constructor(
    private val plugin: MKernel,
    private val configService: ConfigService
) : Listener {

    @EventHandler
    fun onServerLoad(event: ServerLoadEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.Core.MAIN)) return

        val commands = configService.getStringList("settings.startupCommands.onServerLoad")

        commands.forEach { cmd ->
            val cleanCmd = cmd.removePrefix("/")
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cleanCmd)
        }
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.Core.MAIN)) return

        val worldName = event.world.name
        val commands = configService.getStringList("settings.startupCommands.onWorldLoad.$worldName")

        commands.forEach { cmd ->
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.removePrefix("/"))
        }
    }

    @EventHandler
    fun onWorldUnload(event: WorldUnloadEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.Core.MAIN)) return

        val worldName = event.world.name
        val commands = configService.getStringList("settings.startupCommands.onWorldUnload.$worldName")

        commands.forEach { cmd ->
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.removePrefix("/"))
        }
    }
}