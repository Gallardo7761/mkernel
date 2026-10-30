package es.killobyte.nucleo.common.task.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.common.ITask
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Singleton
class LocationTrackerTask @Inject constructor(
    private val plugin: NucleoPlugin
) : ITask {
    private val locations = ConcurrentHashMap<UUID, Location>()
    private var task: BukkitTask? = null

    override fun start() {
        task = object : BukkitRunnable() {
            override fun run() {
                val firstWorldName = Bukkit.getServer().worlds.firstOrNull()?.name ?: return
                for (player in Bukkit.getOnlinePlayers()) {
                    if (player.world.name == firstWorldName) {
                        locations[player.uniqueId] = player.location
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20)
    }

    override fun stop() {
        task?.cancel()
        task = null
        locations.clear()
    }

    fun getPlayerRealTimeLocation(player: Player): Location? {
        return locations[player.uniqueId]
    }

    fun forget(player: Player) {
        locations.remove(player.uniqueId)
    }
}