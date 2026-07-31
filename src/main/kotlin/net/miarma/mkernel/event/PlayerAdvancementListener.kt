package net.miarma.mkernel.event

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.service.impl.DatabaseService
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.advancement.Advancement
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent

@Singleton
class PlayerAdvancementListener @Inject constructor(
    private val plugin: MKernel,
    private val databaseService: DatabaseService
) : Listener {

    @EventHandler
    fun onAdvancementReached(event: PlayerAdvancementDoneEvent) {
        val player = event.player
        val advancement = event.advancement
        val key = advancement.key.key
        val world = player.world

        when {
            key.equals("story/enter_the_nether", true) || key.equals("nether/root", true) -> {
                if (world.environment == World.Environment.NETHER) {
                    databaseService.isWorldBlocked(world.name) { isBlocked ->
                        if (isBlocked) {
                            Bukkit.getScheduler().runTask(plugin, Runnable { revokeAdvancement(player, advancement) })
                        }
                    }
                }
            }
            key.equals("story/enter_the_end", true) || key.equals("end/root", true) -> {
                if (world.environment == World.Environment.THE_END) {
                    databaseService.isWorldBlocked(world.name) { isBlocked ->
                        if (isBlocked) {
                            Bukkit.getScheduler().runTask(plugin, Runnable { revokeAdvancement(player, advancement) })
                        }
                    }
                }
            }
        }
    }

    private fun revokeAdvancement(player: Player, advancement: Advancement) {
        val progress = player.getAdvancementProgress(advancement)
        progress.awardedCriteria.forEach { progress.revokeCriteria(it) }
    }
}
