package net.miarma.mkernel.miarmacraft.event

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.miarmacraft.common.config.ConfigKeys
import net.miarma.mkernel.util.delayTicks
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Warden
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

@Singleton
class BlindGodListener @Inject constructor(
    private val plugin: MKernel,
    private val messageService: MessageService,
    private val configService: ConfigService
) : Listener {
    private val deepMiningCount = ConcurrentHashMap<UUID, Int>()

    @EventHandler
    fun onDeepMining(event: BlockBreakEvent) {
        val player = event.player
        val block = event.block

        if (block.y > configService.getInt(ConfigKeys.Settings.BlindGod.Y_THRESHOLD)) return
        if (!block.type.name.contains("DEEPSLATE")) return

        val currentCount = deepMiningCount.getOrDefault(player.uniqueId, 0) + 1
        deepMiningCount[player.uniqueId] = currentCount

        when (currentCount) {
            configService.getInt(ConfigKeys.Settings.BlindGod.FIRST_VALUE) -> {
                player.playSound(player.location, Sound.ENTITY_WARDEN_AMBIENT, 1.0f, 1.0f)
                player.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 100, 1))
                messageService.builder(configService.getString(ConfigKeys.Messages.BlindGod.FIRST))
                    .send(player)
            }

            configService.getInt(ConfigKeys.Settings.BlindGod.SECOND_VALUE) -> {
                player.playSound(player.location, Sound.ENTITY_WARDEN_HEARTBEAT, 2.0f, 0.8f)
                player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 100, 1))
                messageService.builder(configService.getString(ConfigKeys.Messages.BlindGod.SECOND))
                    .send(player)
            }

            configService.getInt(ConfigKeys.Settings.BlindGod.THIRD_VALUE) -> {
                val wardenLoc = player.location.clone()
                val warden = player.world.spawnEntity(wardenLoc, EntityType.WARDEN) as Warden

                warden.pose = org.bukkit.entity.Pose.EMERGING
                warden.setAI(false)
                warden.isInvulnerable = true

                player.playSound(wardenLoc, Sound.ENTITY_WARDEN_EMERGE, 1.0f, 1.0f)
                messageService.builder(configService.getString(ConfigKeys.Messages.BlindGod.THIRD))
                    .send(player)

                player.world.spawnParticle(Particle.SCULK_SOUL, wardenLoc, 50, 0.5, 0.5, 0.5, 0.05)

                plugin.launchSync {
                    delayTicks(plugin, 134L)

                    if (!warden.isDead) {
                        warden.pose = org.bukkit.entity.Pose.STANDING
                        warden.setAI(true)
                        warden.isInvulnerable = false

                        warden.setAnger(player, 150)
                        warden.target = player

                        player.playSound(wardenLoc, Sound.ENTITY_WARDEN_ROAR, 1.0f, 1.0f)
                    }
                }

                deepMiningCount[player.uniqueId] = 0
            }
        }
    }
}