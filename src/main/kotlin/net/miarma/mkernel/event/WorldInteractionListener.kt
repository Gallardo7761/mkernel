package net.miarma.mkernel.event

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.miarma.mkernel.common.integration.impl.GriefPreventionHook
import net.miarma.mkernel.common.integration.impl.MinepacksHook
import net.miarma.mkernel.common.service.impl.*
import net.miarma.mkernel.event.helper.BlockEventHelper
import net.miarma.mkernel.task.LocationTrackerTask
import net.miarma.mkernel.util.delayTicks
import org.bukkit.EntityEffect
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.*
import org.bukkit.event.world.PortalCreateEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

@Singleton
class WorldInteractionListener @Inject constructor(
    private val plugin: MKernel,
    private val configService: ConfigService,
    private val hookService: HookService,
    private val playerService: PlayerService,
    private val lastPositionService: LastPositionService,
    private val databaseService: DatabaseService,
    private val messageService: MessageService,
    private val inventoryService: InventoryService,
    private val locationTrackerTask: LocationTrackerTask
) : Listener {

    @EventHandler
    fun onRightClick(event: PlayerInteractEvent) {
        if (!configService.isModuleEnabled("harvestOnRightClick") || event.action != Action.RIGHT_CLICK_BLOCK) return
        val hasAccess = hookService.getHook(GriefPreventionHook::class.java).map { it.hasAccess(event.player, event.player.location) }.orElse(true)
        if (hasAccess) {
            event.clickedBlock?.let { block ->
                val helper = BlockEventHelper.of(event.player, block)
                when (block.type) {
                    Material.WHEAT -> helper.handleWheat()
                    Material.POTATOES -> helper.handlePotatoes()
                    Material.CARROTS -> helper.handleCarrots()
                    Material.BEETROOTS -> helper.handleBeetroots()
                    Material.COCOA -> helper.handleCocoa()
                    Material.TORCHFLOWER_CROP -> helper.handleTorchflower()
                    Material.PITCHER_CROP -> helper.handlePitcher()
                    else -> {}
                }
            }
        }
    }

    @EventHandler
    fun onEntityRightClick(event: PlayerInteractEntityEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        val player = event.player
        val item = player.inventory.itemInMainHand
        val entity = event.rightClicked

        when {
            entity.type == EntityType.SKELETON && item.type == Material.ROTTEN_FLESH -> handleRottenFleshOnSkeleton(player, item, entity)
            entity.type == EntityType.PILLAGER && item.type == Material.TOTEM_OF_UNDYING -> handleTotemOnPillager(player, item, entity)
        }
    }

    @EventHandler
    fun onCampfireCook(event: BlockCookEvent) {
        if (event.block.type == Material.CAMPFIRE && event.source.type == Material.ROTTEN_FLESH) {
            event.result = ItemStack(if (Math.random() > 0.40) Material.BEEF else Material.BONE)
        }
    }

    @EventHandler
    fun onPortalEnter(event: PlayerPortalEvent) {
        val toWorld = event.to.world ?: return
        val player = event.player
        val fromLoc = locationTrackerTask.getPlayerRealTimeLocation(player) ?: player.location

        event.isCancelled = true

        plugin.launchSync {
            val blockedWorlds = databaseService.getBlockedWorlds()

            if (!player.isOnline) return@launchSync

            if (blockedWorlds.contains(toWorld.name)) {
                val pushBackLoc = fromLoc.clone().subtract(2.0, 0.0, 2.0)
                player.teleportAsync(pushBackLoc)
                messageService.builder(configService.getString("language.errors.worldIsBlocked"))
                    .withPrefix()
                    .tag("world", toWorld.name)
                    .send(player)
            } else {
                lastPositionService.setLastPosition(player, fromLoc)
                player.teleportAsync(event.to)
            }
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (!configService.isModuleEnabled("autoItemRefill") || event.itemInHand.amount != 1) return
        plugin.launchSync {
            delayTicks(plugin, 1L)
            handleRefill(event.player, event.blockPlaced.type, event.hand)
        }
    }

    @EventHandler
    fun onItemBreak(event: PlayerItemBreakEvent) {
        if (!configService.isModuleEnabled("autoItemRefill")) return
        val player = event.player
        val brokenItem = event.brokenItem
        val hand = if (player.inventory.itemInMainHand.type.isAir || player.inventory.itemInMainHand.type != brokenItem.type) EquipmentSlot.OFF_HAND else EquipmentSlot.HAND
        handleRefill(player, brokenItem.type, hand)
    }

    @EventHandler
    fun onCampfireCook(event: CampfireStartEvent) {
        if (event.block.type != Material.SOUL_CAMPFIRE) {
            event.totalCookTime = (1.5 * event.totalCookTime).toInt()
        }
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        val item = killer.inventory.itemInMainHand
        if (!item.hasItemMeta() || item.itemMeta.enchants.isEmpty()) return
        if (item.containsEnchantment(Enchantment.LOOTING) && (event.entityType == EntityType.ZOMBIE || event.entityType == EntityType.ZOMBIE_VILLAGER)) {
            if (Math.random() * 100 < 70) {
                event.entity.world.dropItem(event.entity.location, ItemStack(Material.ZOMBIE_HEAD, 1))
            }
        }
    }

    @EventHandler
    fun onPortalLight(event: PortalCreateEvent) {
        if (configService.isModuleEnabled("noNetherPortals")) {
            event.isCancelled = true
            (event.entity as? Player)?.let {
                messageService.builder(configService.getString("language.events.illegalPortal")).withPrefix().send(it)
            }
        }
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (playerService.isFrozen(event.player)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        lastPositionService.setLastPosition(event.player, event.from)
    }

    private fun handleRottenFleshOnSkeleton(player: Player, item: ItemStack, entity: Entity) {
        if (Math.random() * 100 >= 50 && item.amount >= 15) {
            entity.remove()
            item.amount -= 15
            val zombie = entity.world.spawnEntity(entity.location, EntityType.ZOMBIE) as Zombie
            zombie.equipment.setItemInMainHand(ItemStack(Material.BOW))
        }
    }

    private fun handleTotemOnPillager(player: Player, item: ItemStack, entity: Entity) {
        item.amount = 0
        if (Math.random() < 0.15) {
            entity.remove()
            val villager = entity.world.spawnEntity(entity.location, EntityType.VILLAGER) as Villager
            villager.setBaby()
            player.playSound(player.location, Sound.ITEM_TOTEM_USE, 1f, 1f)
            player.playEffect(EntityEffect.PROTECTED_FROM_DEATH)
        }
    }

    private fun handleRefill(player: Player, material: Material, hand: EquipmentSlot) {
        if (inventoryService.getItemCount(player.inventory, material) > 0) {
            inventoryService.refillItem(player, material, hand)
            return
        }
        hookService.getHook(MinepacksHook::class.java).ifPresent { minepacks ->
            val backpack = minepacks.getPlayerBackpackInventory(player)
            if (backpack != null && !backpack.isEmpty) {
                if (backpack.contents.any { it?.type == material }) {
                    inventoryService.refillItemFromMinepack(player, material, hand)
                }
            }
        }
    }
}
