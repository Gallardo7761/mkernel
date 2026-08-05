package net.miarma.mkernel.event

import com.google.inject.Inject
import com.google.inject.Singleton
import de.tr7zw.nbtapi.NBT
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.model.Shop
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.integration.impl.GriefPreventionHook
import net.miarma.mkernel.common.integration.impl.WorldGuardHook
import net.miarma.mkernel.common.inventory.ShopBuyInventory
import net.miarma.mkernel.common.recipe.RecipeLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.HookService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.service.impl.ShopService
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.entity.Enderman
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Singleton
class ShopListener @Inject constructor(
    private val plugin: MKernel,
    private val shopService: ShopService,
    private val messageService: MessageService,
    private val shopBuyInventory: ShopBuyInventory,
    private val configService: ConfigService,
    private val hookService: HookService
) : Listener {

    private val pendingShops = ConcurrentHashMap<UUID, Location>()

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.Shop.MAIN)) return

        val block = event.block
        val item = event.itemInHand
        val player = event.player

        val tag = runCatching {
            NBT.get<String>(item) { it.getString("special_item_tag") }
        }.getOrNull()

        if (tag == "shop_chest") {
            val wgHook = hookService.getHook(WorldGuardHook::class.java).orElse(null)
            val gpHook = hookService.getHook(GriefPreventionHook::class.java).orElse(null)

            if (wgHook != null && gpHook != null && !wgHook.canCreateShop(player, block.location) &&
                    !gpHook.hasAccess(player, block.location)) {
                event.isCancelled = true
                pendingShops.remove(player.uniqueId)
                messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.NO_PERMISSION))
                    .withPrefix()
                    .send(player)
                return
            }

            val chestData = block.blockData as? org.bukkit.block.data.type.Chest
            if (chestData != null) {
                chestData.type = org.bukkit.block.data.type.Chest.Type.SINGLE
                block.blockData = chestData
            }

            pendingShops[player.uniqueId] = block.location
            messageService.builder(configService.getString(ConfigKeys.Messages.Shops.Chat.PLACED))
                .withPrefix()
                .send(player)
            return
        }

        if (block.type == Material.CHEST || block.type == Material.TRAPPED_CHEST) {
            val faces = listOf(
                org.bukkit.block.BlockFace.NORTH,
                org.bukkit.block.BlockFace.SOUTH,
                org.bukkit.block.BlockFace.EAST,
                org.bukkit.block.BlockFace.WEST
            )

            val isAdjacentToShop = faces.any { face ->
                val relativeBlock = block.getRelative(face)
                shopService.getShopAt(relativeBlock.location) != null
            }

            if (isAdjacentToShop) {
                val chestData = block.blockData as? org.bukkit.block.data.type.Chest
                if (chestData != null) {
                    chestData.type = org.bukkit.block.data.type.Chest.Type.SINGLE
                    block.blockData = chestData
                }
            }
        }
    }

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.Shop.MAIN)) return

        val player = event.player
        val loc = pendingShops[player.uniqueId] ?: return

        event.isCancelled = true
        val msg = PlainTextComponentSerializer.plainText().serialize(event.message())
        val price = msg.toDoubleOrNull()

        if (price == null || price <= 0) {
            messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.NOT_A_NUMBER))
                .withPrefix()
                .send(player)
            return
        }

        pendingShops.remove(player.uniqueId)

        plugin.launchSync {
            val block = loc.block
            if (block.type != Material.CHEST && block.type != Material.TRAPPED_CHEST && block.type != Material.BARREL) {
                messageService.builder(ConfigKeys.Messages.Shops.Errors.SHOP_NOT_ACCESSIBLE)
                    .withPrefix()
                    .send(player)
                return@launchSync
            }

            val shopId = shopService.generateShopId(loc)

            val chest = block.state as? Chest
            val firstItem = chest?.blockInventory?.contents?.firstOrNull { it != null && !it.type.isAir }?.clone()?.apply { amount = 1 } ?: ItemStack(Material.DIAMOND) // O un fallback seguro
            val totalStock = chest?.blockInventory?.contents?.filterNotNull()?.sumOf { if (it.isSimilar(firstItem)) it.amount else 0 } ?: 0

            val shop = Shop(
                id = shopId,
                ownerUuid = player.uniqueId,
                location = loc,
                item = firstItem,
                price = price,
                stock = totalStock
            )

            shopService.syncShop(shop)

            messageService.builder(configService.getString(ConfigKeys.Messages.Shops.Chat.CREATED))
                .withPrefix()
                .tag("item", shop.item.type.name)
                .send(player)
        }
    }

    @EventHandler
    fun onShopClick(event: PlayerInteractEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.Shop.MAIN)) return

        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return
        if (block.type != Material.CHEST) return

        val shop = shopService.getShopAt(block.location) ?: return
        val player = event.player

        if (player.uniqueId == shop.ownerUuid) {
            event.isCancelled = false
        } else {
            event.isCancelled = true

            if (shop.item.type.isAir || shop.stock <= 0) {
                messageService.builder(configService.getString(ConfigKeys.Messages.Shops.Errors.NO_STOCK))
                    .withPrefix()
                    .send(player)
                return
            }
            shopBuyInventory.open(player, shop)
        }
    }

    @EventHandler
    fun onChestClose(event: InventoryCloseEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.Shop.MAIN)) return

        val chest = event.inventory.holder as? Chest ?: return
        val shop = shopService.getShopAt(chest.location) ?: return

        if (event.player.uniqueId == shop.ownerUuid) {
            var firstItem: ItemStack? = null
            var totalStock = 0

            for (item in chest.blockInventory.contents) {
                if (item != null && !item.type.isAir) {
                    if (firstItem == null) {
                        firstItem = item.clone().apply { amount = 1 }
                    }
                    if (item.isSimilar(firstItem)) {
                        totalStock += item.amount
                    }
                }
            }

            shop.item = firstItem ?: ItemStack(Material.AIR)
            shop.stock = totalStock
            shopService.syncShop(shop)
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onShopDestroy(event: BlockBreakEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.Shop.MAIN)) return

        val block = event.block
        if (block.type != Material.CHEST && block.type != Material.TRAPPED_CHEST && block.type != Material.BARREL) return

        val player = event.player

        if (pendingShops[player.uniqueId] == block.location) {
            event.isCancelled = true

            val chest = block.state as? Chest
            chest?.blockInventory?.contents?.forEach { item ->
                if (item != null && !item.type.isAir) {
                    block.world.dropItemNaturally(block.location, item)
                }
            }
            chest?.blockInventory?.clear()

            val recipe = Bukkit.getRecipe(NamespacedKey(plugin, "shop_chest"))
            val shopChest = recipe?.result?.clone() ?: ItemStack(Material.CHEST)

            block.world.dropItemNaturally(block.location, shopChest)

            block.type = Material.AIR
            pendingShops.remove(player.uniqueId)

            return
        }

        val shop = shopService.getShopAt(block.location) ?: return

        if (player.uniqueId != shop.ownerUuid && !player.hasPermission(configService.getString(ConfigKeys.Settings.Shops.PERM_ADMIN))) {
            event.isCancelled = true
            messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.NO_PERMISSION))
                .withPrefix()
                .send(player)
            return
        }

        event.isCancelled = true

        val chestState = block.state as? Chest
        chestState?.blockInventory?.contents?.filterNotNull()?.forEach { stack ->
            if (!stack.type.isAir) {
                block.world.dropItemNaturally(block.location, stack)
            }
        }
        chestState?.blockInventory?.clear()

        val recipeKey = NamespacedKey(plugin, "shop_chest")
        val recipe = Bukkit.getRecipe(recipeKey)
        val shopChestItem = recipe?.result?.clone() ?: ItemStack(Material.CHEST)
        block.world.dropItemNaturally(block.location, shopChestItem)

        block.type = Material.AIR
        shopService.deleteShop(shop.id)
        pendingShops.remove(player.uniqueId)

        messageService.builder(configService.getString(ConfigKeys.Messages.Shops.Chat.DESTROY))
            .withPrefix()
            .send(player)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityExplode(event: EntityExplodeEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.Shop.MAIN)) return
        event.blockList().removeIf { block -> isShopBlock(block) }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockExplode(event: BlockExplodeEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.Shop.MAIN)) return
        event.blockList().removeIf { block -> isShopBlock(block) }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityChangeBlock(event: EntityChangeBlockEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.Shop.MAIN)) return

        if (isShopBlock(event.block)) {
            event.isCancelled = true

            if (event.entity is Enderman) {
                (event.entity as Enderman).carriedBlock = null
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockBurn(event: BlockBurnEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.Shop.MAIN)) return

        if (isShopBlock(event.block)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPistonExtend(event: BlockPistonExtendEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.Shop.MAIN)) return

        if (event.blocks.any { isShopBlock(it) }) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPistonRetract(event: BlockPistonRetractEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.Shop.MAIN)) return

        if (event.blocks.any { isShopBlock(it) }) {
            event.isCancelled = true
        }
    }

    private fun isShopBlock(block: Block): Boolean {
        if (block.type != Material.CHEST && block.type != Material.TRAPPED_CHEST && block.type != Material.BARREL) {
            return false
        }

        return shopService.getShopAt(block.location) != null
    }
}