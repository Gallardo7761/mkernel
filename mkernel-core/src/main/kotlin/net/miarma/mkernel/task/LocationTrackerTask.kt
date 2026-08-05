package net.miarma.mkernel.task

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.MKernel
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.ConcurrentHashMap

@Singleton
class LocationTrackerTask @Inject constructor(private val plugin: MKernel) {
    private val locations = ConcurrentHashMap<Player, Location>()

    fun start() {
        object : BukkitRunnable() {
            override fun run() {
                val firstWorldName = Bukkit.getServer().worlds.firstOrNull()?.name ?: return
                for (player in Bukkit.getOnlinePlayers()) {
                    if (player.world.name == firstWorldName) {
                        locations[player] = player.location
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20)
    }

    fun getPlayerRealTimeLocation(player: Player): Location? {
        return locations[player]
    }
}