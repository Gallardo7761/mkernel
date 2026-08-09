package net.miarma.mkernel.miarmacraft.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.common.IService
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.HookService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.miarmacraft.common.config.ConfigKeys
import net.miarma.mkernel.miarmacraft.common.dao.CrimeDao
import net.miarma.mkernel.miarmacraft.common.integration.impl.LevelledMobsHook
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.IronGolem
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Singleton
class LawEnforcementService @Inject constructor(
    private val plugin: MKernel,
    private val configService: ConfigService,
    private val hookService: HookService,
    private val messageService: MessageService,
    private val crimeDao: CrimeDao
) : IService {

    private val policeKey = NamespacedKey(plugin, "is_police")
    private lateinit var policeCooldowns: ConcurrentHashMap<UUID, Long>

    override fun onEnable() {
        policeCooldowns = ConcurrentHashMap<UUID, Long>()
    }

    override fun onDisable() {
        policeCooldowns.clear()
    }

    fun isTownZone(loc: Location): Boolean {
        val wgContainer = WorldGuard.getInstance().platform.regionContainer
        val query = wgContainer.createQuery()
        val set = query.getApplicableRegions(BukkitAdapter.adapt(loc))
        val regionName = configService.getString(ConfigKeys.Settings.LawEnforcement.REGION)
        return set.regions.any { it.id.equals(regionName, ignoreCase = true) }
    }

    fun isPolice(entity: Entity): Boolean {
        return entity is IronGolem && entity.persistentDataContainer.has(policeKey, PersistentDataType.BYTE)
    }

    fun dispatchPolice(player: Player, crimeCount: Int) {
        val now = System.currentTimeMillis()
        val lastDispatch = policeCooldowns[player.uniqueId] ?: 0L

        if (now - lastDispatch < 1000) {
            return
        }

        policeCooldowns[player.uniqueId] = now

        player.playSound(player.location, Sound.BLOCK_BELL_USE, 1.0f, 0.8f)

        val spawnDelay = configService.getInt(ConfigKeys.Settings.LawEnforcement.SPAWN_DELAY, 4)
        val aggroTime = configService.getInt(ConfigKeys.Settings.LawEnforcement.AGGRO_TIME)
        val despawnTime = configService.getInt(ConfigKeys.Settings.LawEnforcement.DESPAWN_TIME)
        val maxGolems = configService.getInt(ConfigKeys.Settings.LawEnforcement.MAX_GOLEMS, 6)

        object : BukkitRunnable() {
            override fun run() {
                if (!player.isOnline) return

                player.playSound(player.location, Sound.ENTITY_IRON_GOLEM_DEATH, 1.0f, 0.5f)

                var existingGolems = 0
                player.location.chunk.entities.forEach { entity ->
                    if (isPolice(entity)) {
                        existingGolems++
                        (entity as IronGolem).target = player
                    }
                }

                val toSpawn = minOf(3, maxGolems - existingGolems)

                if (toSpawn > 0) {
                    val golemNameComp = messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.GOLEM_NAME)).build()
                    val baseLevel = configService.getInt(ConfigKeys.Settings.LawEnforcement.GOLEM_BASE_LEVEL, 20)
                    val multiplier = configService.getDouble(ConfigKeys.Settings.LawEnforcement.GOLEM_LEVEL_MULTIPLIER, 1.12)
                    val lmHook = hookService.getHook(LevelledMobsHook::class.java).orElse(null)

                    repeat(toSpawn) {
                        val golemLoc = player.location.clone().add((Math.random() * 8) - 2, 1.0, (Math.random() * 8) - 2)
                        val golem = player.world.spawnEntity(golemLoc, EntityType.IRON_GOLEM) as IronGolem

                        golem.persistentDataContainer.set(policeKey, PersistentDataType.BYTE, 1.toByte())
                        golem.customName(golemNameComp)
                        golem.isCustomNameVisible = true

                        if (lmHook != null) {
                            lmHook.applyPoliceLevel(golem, baseLevel, multiplier, crimeCount)
                        } else {
                            golem.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 9999, 1))
                            golem.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, 9999, 3))
                        }

                        golem.damage(0.0, player)

                        object : BukkitRunnable() {
                            var ticksPassed = 0
                            override fun run() {
                                if (golem.isDead || ticksPassed >= (aggroTime * 20)) {
                                    cancel()
                                    return
                                }
                                if (golem.target != player) {
                                    golem.target = player
                                }
                                ticksPassed += 20
                            }
                        }.runTaskTimer(plugin, 0L, 20L)

                        object : BukkitRunnable() {
                            override fun run() {
                                if (!golem.isDead) {
                                    golem.world.spawnParticle(Particle.CLOUD, golem.location.clone().add(0.0, 1.0, 0.0), 30, 0.5, 0.5, 0.5, 0.1)
                                    golem.remove()
                                }
                            }
                        }.runTaskLater(plugin, (despawnTime * 20L))
                    }
                }
            }
        }.runTaskLater(plugin, (spawnDelay * 20L))
    }
}