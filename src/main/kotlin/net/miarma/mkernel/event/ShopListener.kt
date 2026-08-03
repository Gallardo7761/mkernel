package net.miarma.mkernel.event

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import de.tr7zw.nbtapi.NBT
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.miarma.mkernel.common.integration.impl.WorldGuardHook
import net.miarma.mkernel.common.inventory.ShopBuyInventory
import net.miarma.mkernel.common.model.Shop
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.HookService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.service.impl.ShopService
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
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
        val block = event.block
        val item = event.itemInHand
        val player = event.player

        val tag = runCatching {
            NBT.get<String>(item) { it.getString("special_item_tag") }
        }.getOrNull()

        if (tag == "shop_chest") {
            val wgHook = hookService.getHook(WorldGuardHook::class.java).orElse(null)
            if (wgHook != null && !wgHook.canCreateShop(player, block.location)) {
                event.isCancelled = true
                pendingShops.remove(player.uniqueId)
                messageService.builder(configService.getString("language.errors.noPermission"))
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
            messageService.builder(configService.getString("language.events.onShop.placed"))
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
        val player = event.player
        val loc = pendingShops[player.uniqueId] ?: return

        event.isCancelled = true
        val msg = PlainTextComponentSerializer.plainText().serialize(event.message())
        val price = msg.toDoubleOrNull()

        if (price == null || price <= 0) {
            messageService.builder(configService.getString("language.errors.notANumber"))
                .withPrefix()
                .send(player)
            return
        }

        pendingShops.remove(player.uniqueId)

        plugin.launchSync {
            val shop = Shop(shopService.generateShopId(loc), player.uniqueId, loc, ItemStack(Material.AIR), price, 0)
            shopService.syncShop(shop)
            messageService.builder(configService.getString("language.events.onShop.created"))
                .withPrefix()
                .tag("item", shop.item.type.name)
                .send(player)
        }
    }

    @EventHandler
    fun onShopClick(event: PlayerInteractEvent) {
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
                messageService.builder(configService.getString("language.errors.noStock"))
                    .withPrefix()
                    .send(player)
                return
            }
            shopBuyInventory.open(player, shop)
        }
    }

    @EventHandler
    fun onChestClose(event: InventoryCloseEvent) {
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

    @EventHandler
    fun onShopDestroy(event: BlockBreakEvent) {
        val block = event.block
        if (block.type != Material.CHEST && block.type != Material.TRAPPED_CHEST && block.type != Material.BARREL) return

        val player = event.player

        if (pendingShops[player.uniqueId] == block.location) {
            pendingShops.remove(player.uniqueId)
        }

        val shop = shopService.getShopAt(block.location) ?: return

        if (player.uniqueId != shop.ownerUuid && !player.hasPermission(configService.getString("config.permissions.shop.admin"))) {
            event.isCancelled = true
            messageService.builder(configService.getString("language.errors.noPermission"))
                .withPrefix()
                .send(player)
            return
        }

        shopService.deleteShop(shop.id)
        pendingShops.remove(player.uniqueId)

        messageService.builder(configService.getString("language.events.onShop.destroy"))
            .withPrefix()
            .send(player)
    }
}