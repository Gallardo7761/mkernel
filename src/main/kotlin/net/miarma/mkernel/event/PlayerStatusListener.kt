package net.miarma.mkernel.event

import com.google.inject.Inject
import com.google.inject.Singleton
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.DatabaseService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.service.impl.PlayerService
import net.miarma.mkernel.util.PlayerUtil
import net.miarma.mkernel.util.PlayerUtil.getNickName
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import java.time.Duration

@Singleton
class PlayerStatusListener @Inject constructor(
    private val configService: ConfigService,
    private val databaseService: DatabaseService,
    private val messageService: MessageService,
    private val playerService: PlayerService
) : Listener {

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        val playerLevel = player.level
        val playerExp = player.exp

        if (configService.isModuleEnabled(ConfigKeys.Modules.Player.DEATH_TITLE)) {
            val rawSubtitle = configService.getString(ConfigKeys.Messages.General.Titles.DEATH)
            val times = Title.Times.times(Duration.ofMillis(1500), Duration.ofMillis(1500), Duration.ofMillis(1500))

            Bukkit.getOnlinePlayers().forEach { p ->
                p.playSound(p.location, Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f)
                val subTitle = messageService.builder(rawSubtitle)
                    .tag("player", player.getNickName(playerService))
                    .build()
                p.showTitle(Title.title(Component.empty(), subTitle, times))
            }
        }

        if (configService.isModuleEnabled(ConfigKeys.Modules.Player.RECOVER_INVENTORY)) {
            val deathLocation = player.location
            val playerSpawnPoint = player.respawnLocation ?: player.world.spawnLocation
            val onlinePlayers = Bukkit.getOnlinePlayers()

            if (deathLocation.distance(playerSpawnPoint) <= configService.getInt(ConfigKeys.Settings.Death.REC_INV_SPAWN_DIST) ||
                PlayerUtil.playersNearRadius(player, onlinePlayers, configService.getInt(ConfigKeys.Settings.Death.REC_INV_RADIUS))) {
                messageService.builder(configService.getString(ConfigKeys.Messages.Death.ITEMS_NOT_RECOVERED))
                    .withPrefix()
                    .forPlayer(player)
                    .tag("x", deathLocation.blockX.toString())
                    .tag("y", deathLocation.blockY.toString())
                    .tag("z", deathLocation.blockZ.toString())
                    .send(player)
            } else {
                databaseService.saveInventory(player.uniqueId.toString(), player.inventory.contents)
                event.drops.clear()
                event.droppedExp = 0
                event.keepInventory = true
                player.inventory.armorContents = arrayOfNulls(4)
                player.inventory.clear()
                player.updateInventory()

                val xpLossOnDeath = configService.getFloat(ConfigKeys.Settings.Death.XP_LOSS)
                val levelsToLose = (playerLevel * xpLossOnDeath).toInt()
                val newLevel = (playerLevel - levelsToLose).coerceAtLeast(0)

                event.newLevel = newLevel
                event.newExp = playerExp.toInt()

                messageService.builder(configService.getString(ConfigKeys.Messages.Death.LOST_LEVELS_ITEMS))
                    .withPrefix()
                    .tag("levels", levelsToLose.toString())
                    .send(player)
            }
        }
    }
}
