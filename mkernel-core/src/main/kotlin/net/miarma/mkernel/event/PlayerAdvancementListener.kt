package net.miarma.mkernel.event

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.common.dao.WorldDao
import org.bukkit.World
import org.bukkit.advancement.Advancement
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent

@Singleton
class PlayerAdvancementListener @Inject constructor(
    private val plugin: MKernel,
    private val worldDao: WorldDao
) : Listener {

    @EventHandler
    fun onAdvancementReached(event: PlayerAdvancementDoneEvent) {
        val player = event.player
        val advancement = event.advancement
        val key = advancement.key.key
        val world = player.world

        val isNether = (key.equals("story/enter_the_nether", true) || key.equals("nether/root", true)) && world.environment == World.Environment.NETHER
        val isEnd = (key.equals("story/enter_the_end", true) || key.equals("end/root", true)) && world.environment == World.Environment.THE_END

        if (!isNether && !isEnd) return

        plugin.launchSync {
            val isBlocked = worldDao.isWorldBlocked(world.name)

            if (isBlocked) {
                revokeAdvancement(player, advancement)
            }
        }
    }

    private fun revokeAdvancement(player: Player, advancement: Advancement) {
        val progress = player.getAdvancementProgress(advancement)
        progress.awardedCriteria.forEach { progress.revokeCriteria(it) }
    }
}
