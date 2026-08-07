package net.miarma.mkernel.miarmacraft.event

import com.google.inject.Inject
import com.google.inject.Singleton
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.common.integration.impl.GriefPreventionHook
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.HookService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.miarmacraft.common.config.ConfigKeys
import net.miarma.mkernel.miarmacraft.common.dao.CrimeDao
import net.miarma.mkernel.util.delayTicks
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

@Singleton
class LawEnforcementListener @Inject constructor(
    private val plugin: MKernel,
    private val configService: ConfigService,
    private val hookService: HookService,
    private val messageService: MessageService,
    private val crimeDao: CrimeDao
) : Listener {

    private val policeKey = NamespacedKey(plugin, "is_police")

    private fun isTownZone(loc: org.bukkit.Location): Boolean {
        val wgContainer = WorldGuard.getInstance().platform.regionContainer
        val query = wgContainer.createQuery()
        val set = query.getApplicableRegions(BukkitAdapter.adapt(loc))
        val regionName = configService.getString(ConfigKeys.Settings.LawEnforcement.REGION)
        return set.regions.any { it.id.equals(regionName, ignoreCase = true) }
    }

    private fun isProtectedZone(player: Player, loc: org.bukkit.Location): Boolean {
        if (isTownZone(loc)) return true
        val gp = hookService.getHook(GriefPreventionHook::class.java).orElse(null)
        if (gp != null && !gp.hasAccess(player, loc)) {
            return true
        }
        return false
    }

    private fun isImmune(player: Player): Boolean {
        val perm = configService.getString(ConfigKeys.Settings.LawEnforcement.IMMUNITY_PERM)
        val immune = player.hasPermission(perm)
        if (immune) {
            plugin.logger.info("[Dictadura] El jugador ${player.name} es inmune a la ley.")
        }
        return immune
    }

    private fun getRealAttacker(damager: Entity): Player? {
        if (damager is Player) return damager
        if (damager is Projectile && damager.shooter is Player) return damager.shooter as Player
        return null
    }

    private fun isPolice(entity: Entity): Boolean {
        return entity is IronGolem && entity.persistentDataContainer.has(policeKey, PersistentDataType.BYTE)
    }

    private fun dispatchPolice(player: Player, crime: String) {
        plugin.logger.info("[Dictadura] Desplegando legión contra ${player.name} por: $crime")
        plugin.launchAsync {
            crimeDao.insertCrime(player, crime)
        }

        val warningMsg = configService.getString(ConfigKeys.Messages.LawEnforcement.WARNING)
        messageService.builder(warningMsg).tag("crime", crime).send(player)
        player.playSound(player.location, Sound.BLOCK_BELL_USE, 1.0f, 0.8f)

        val spawnDelay = configService.getInt(ConfigKeys.Settings.LawEnforcement.SPAWN_DELAY, 4)
        val aggroTime = configService.getInt(ConfigKeys.Settings.LawEnforcement.AGGRO_TIME)
        val despawnTime = configService.getInt(ConfigKeys.Settings.LawEnforcement.DESPAWN_TIME)
        val maxGolems = configService.getInt(ConfigKeys.Settings.LawEnforcement.MAX_GOLEMS, 6)

        plugin.launchSync {
            delayTicks(plugin, spawnDelay * 20L)

            if (!player.isOnline) return@launchSync

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

                repeat(toSpawn) {
                    val golemLoc = player.location.clone().add((Math.random() * 4) - 2, 1.0, (Math.random() * 4) - 2)
                    val golem = player.world.spawnEntity(golemLoc, EntityType.IRON_GOLEM) as IronGolem

                    golem.persistentDataContainer.set(policeKey, PersistentDataType.BYTE, 1.toByte())
                    golem.customName(golemNameComp)
                    golem.isCustomNameVisible = true

                    golem.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 9999, 1))
                    golem.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, 9999, 0))
                    golem.addPotionEffect(PotionEffect(PotionEffectType.HEALTH_BOOST, 9999, 2))
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

                    plugin.launchSync {
                        delayTicks(plugin, despawnTime * 20L)
                        if (!golem.isDead) {
                            golem.world.spawnParticle(org.bukkit.Particle.CLOUD, golem.location.clone().add(0.0, 1.0, 0.0), 30, 0.5, 0.5, 0.5, 0.1)
                            golem.remove()
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    fun onPoliceDeath(event: EntityDeathEvent) {
        if (isPolice(event.entity)) {
            event.drops.clear()
            event.droppedExp = 0
        }
    }

    @EventHandler
    fun onIllegalPlacement(event: BlockPlaceEvent) {
        if (isImmune(event.player)) return
        val block = event.block

        val illegalBlocks = listOf(
            Material.TNT, Material.SPAWNER, Material.FIRE, Material.SOUL_FIRE,
            Material.RESPAWN_ANCHOR
        )

        if (illegalBlocks.contains(block.type)) {
            if (isProtectedZone(event.player, block.location)) {
                event.isCancelled = true
                dispatchPolice(event.player, "Art. V — Posesión de explosivos")
            }
        }
    }

    @EventHandler
    fun onIllegalBucket(event: PlayerBucketEmptyEvent) {
        if (isImmune(event.player)) return

        if (event.bucket == Material.LAVA_BUCKET || event.bucket == Material.WATER_BUCKET) {
            if (isProtectedZone(event.player, event.block.location)) {
                event.isCancelled = true
                dispatchPolice(event.player, "Art. IV — Daños a propiedad (Líquidos)")
            }
        }
    }

    @EventHandler
    fun onIllegalInteract(event: PlayerInteractEvent) {
        if (isImmune(event.player)) return

        if (event.action == Action.PHYSICAL && event.clickedBlock?.type == Material.FARMLAND) {
            if (isProtectedZone(event.player, event.clickedBlock!!.location)) {
                event.isCancelled = true
                dispatchPolice(event.player, "Art. IV — Daños a propiedad (Cultivos)")
                return
            }
        }

        if (event.item?.type == Material.FLINT_AND_STEEL || event.item?.type == Material.FIRE_CHARGE || event.item?.type == Material.END_CRYSTAL) {
            if (event.action == Action.RIGHT_CLICK_BLOCK && event.clickedBlock?.let { isProtectedZone(event.player, it.location) } == true) {
                event.isCancelled = true
                dispatchPolice(event.player, "Art. IV — Daños a propiedad (Vandalismo)")
                return
            }
        }

        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return

        val storageBlocks = listOf(
            Material.CHEST, Material.BARREL, Material.TRAPPED_CHEST,
            Material.FURNACE, Material.SMOKER, Material.BLAST_FURNACE, Material.HOPPER, Material.DISPENSER, Material.DROPPER
        )

        if (storageBlocks.contains(block.type) || block.type.name.contains("SHULKER_BOX")) {
            if (isProtectedZone(event.player, block.location)) {
                event.isCancelled = true
                dispatchPolice(event.player, "Art. I — Hurto simple")
            }
        }
    }

    @EventHandler
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        if (isImmune(event.player)) return
        val entity = event.rightClicked

        if (entity is ItemFrame || entity is GlowItemFrame) {
            if (isProtectedZone(event.player, entity.location)) {
                event.isCancelled = true
                dispatchPolice(event.player, "Art. I — Hurto simple (Expositores)")
            }
        }
    }

    @EventHandler
    fun onArmorStandInteract(event: PlayerArmorStandManipulateEvent) {
        if (isImmune(event.player)) return
        if (isProtectedZone(event.player, event.rightClicked.location)) {
            event.isCancelled = true
            dispatchPolice(event.player, "Art. I — Hurto simple (Armaduras)")
        }
    }

    @EventHandler
    fun onHangingBreak(event: HangingBreakByEntityEvent) {
        val attacker = getRealAttacker(event.remover ?: return) ?: return
        if (isImmune(attacker)) return

        if (isProtectedZone(attacker, event.entity.location)) {
            event.isCancelled = true
            dispatchPolice(attacker, "Art. IV — Daños a propiedad (Decoración)")
        }
    }

    @EventHandler
    fun onAssault(event: EntityDamageByEntityEvent) {
        if (event.damage <= 0.0) return

        val victim = event.entity
        val attacker = getRealAttacker(event.damager) ?: return

        if (isImmune(attacker)) return

        if (victim is Player) {
            if (isTownZone(victim.location)) {
                event.isCancelled = true
                dispatchPolice(attacker, "Art. I — Homicidio / Agresión en zona segura")
            }
        } else if (victim is Villager || victim is WanderingTrader || victim is IronGolem) {
            if (isPolice(victim)) return

            if (isProtectedZone(attacker, victim.location)) {
                event.isCancelled = true
                dispatchPolice(attacker, "Art. III — Atentado a la autoridad / Civil")
            }
        } else if (victim is Tameable && victim.isTamed) {
            if (isProtectedZone(attacker, victim.location)) {
                event.isCancelled = true
                dispatchPolice(attacker, "Art. V — Daño a bienes ganaderos (Mascotas)")
            }
        } else if (victim is Animals || victim is WaterMob) {
            if (isProtectedZone(attacker, victim.location)) {
                event.isCancelled = true
                dispatchPolice(attacker, "Art. V — Daño a bienes ganaderos")
            }
        } else if (victim is Vehicle) {
            if (isProtectedZone(attacker, victim.location)) {
                event.isCancelled = true
                dispatchPolice(attacker, "Art. IV — Daños a propiedad (Vehículos)")
            }
        }
    }
}