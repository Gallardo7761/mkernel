package net.miarma.mkernel.miarmacraft.event

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.miarmacraft.common.config.ConfigKeys
import net.miarma.mkernel.miarmacraft.common.service.impl.LawEnforcementService
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Animals
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerInteractEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Singleton
class TownProtectionListener @Inject constructor(
    private val plugin: MKernel,
    private val messageService: MessageService,
    private val lawEnforcementService: LawEnforcementService,
    private val configService: ConfigService
) : Listener {

    private val BUILD_PERM = configService.getString(ConfigKeys.Settings.LawEnforcement.BUILD_PERM)
    private val USE_PERM = configService.getString(ConfigKeys.Settings.LawEnforcement.USE_PERM)
    private val HARM_PERM = configService.getString(ConfigKeys.Settings.LawEnforcement.HARM_PERM)

    private val griefCounter = ConcurrentHashMap<UUID, Int>()
    private val openableContainers = setOf(
        Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL,
        Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER,
        Material.SHULKER_BOX, Material.COPPER_CHEST, Material.EXPOSED_COPPER_CHEST,
        Material.OXIDIZED_COPPER_CHEST, Material.WAXED_COPPER_CHEST, Material.WEATHERED_COPPER_CHEST,
        Material.WAXED_EXPOSED_COPPER_CHEST, Material.WAXED_OXIDIZED_COPPER_CHEST, Material.WAXED_WEATHERED_COPPER_CHEST
    )

    private fun checkAndPunish(player: Player, action: String) {
        val count = griefCounter.getOrDefault(player.uniqueId, 0) + 1
        griefCounter[player.uniqueId] = count

        when {
            count in 1..2 -> {
                messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.MSG_FIRST_PUNISHMENT))
                    .withPrefix()
                    .tag("action", action)
                    .send(player)
            }
            count in 3..4 -> {
                messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.MSG_SECOND_PUNISHMENT))
                    .withPrefix()
                    .tag("action", action)
                    .send(player)
            }
            count in 5..9 -> {
                messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.MSG_THIRD_PUNISHMENT))
                    .withPrefix()
                    .send(player)
                plugin.launchSync {
                    lawEnforcementService.dispatchPolice(player, count / 2)
                }
            }
            count >= 10 -> {
                val tempBanDuration = configService.getString(ConfigKeys.Settings.LawEnforcement.TEMP_BAN_DURATION)
                val tempBanMsg = configService.getString(ConfigKeys.Messages.LawEnforcement.MSG_TEMP_BAN)
                lawEnforcementService.tempBanIfNotAlready(player.name, player.uniqueId, tempBanDuration, tempBanMsg)
                griefCounter.remove(player.uniqueId)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.LawEnforcement.MAIN)) return

        val player = event.player
        if (lawEnforcementService.isTownZone(event.block.location)) {
            if (!player.hasPermission(BUILD_PERM) && !player.isOp) {
                event.isCancelled = true
                checkAndPunish(player, "romper bloques")
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.LawEnforcement.MAIN)) return

        val player = event.player
        if (lawEnforcementService.isTownZone(event.block.location)) {
            if (!player.hasPermission(BUILD_PERM) && !player.isOp) {
                event.isCancelled = true
                checkAndPunish(player, "construir")
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    fun onBucketEmpty(event: PlayerBucketEmptyEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.LawEnforcement.MAIN)) return

        val player = event.player
        if (lawEnforcementService.isTownZone(event.block.location)) {
            if (!player.hasPermission(BUILD_PERM) && !player.isOp) {
                event.isCancelled = true
                checkAndPunish(player, "verter líquidos")
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    fun onContainerInteract(event: PlayerInteractEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.LawEnforcement.MAIN)) return
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val block = event.clickedBlock ?: return
        if (block.type !in openableContainers) return

        val player = event.player
        if (lawEnforcementService.isTownZone(block.location)) {
            if (!player.hasPermission(USE_PERM) && !player.isOp) {
                event.isCancelled = true
                checkAndPunish(player, "acceder a contenedores")
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    fun onBlockIgnite(event: BlockIgniteEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.LawEnforcement.MAIN)) return

        val player = event.player ?: return
        if (lawEnforcementService.isTownZone(event.block.location)) {
            if (!player.hasPermission(BUILD_PERM) && !player.isOp) {
                event.isCancelled = true
                checkAndPunish(player, "prender fuego")
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    fun onEntityHarm(event: EntityDamageByEntityEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.LawEnforcement.MAIN)) return

        val victim = event.entity
        if (victim !is Animals && victim !is Villager) return

        val player = event.damager as? Player ?: return
        if (lawEnforcementService.isTownZone(victim.location)) {
            if (!player.hasPermission(HARM_PERM) && !player.isOp) {
                event.isCancelled = true
                checkAndPunish(player, "atacar animales o ciudadanos")
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityExplode(event: EntityExplodeEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.LawEnforcement.MAIN)) return
        if (configService.getBoolean(ConfigKeys.Settings.LawEnforcement.EXPLOSIONS_ENABLED, true)) return

        event.blockList().removeIf { lawEnforcementService.isTownZone(it.location) }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockExplode(event: BlockExplodeEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.LawEnforcement.MAIN)) return
        if (configService.getBoolean(ConfigKeys.Settings.LawEnforcement.EXPLOSIONS_ENABLED, true)) return

        event.blockList().removeIf { lawEnforcementService.isTownZone(it.location) }
    }
}