package net.miarma.mkernel.common.inventory

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.integration.impl.BancoHook
import net.miarma.mkernel.common.model.Shop
import net.miarma.mkernel.common.service.impl.*
import net.miarma.mkernel.util.PlayerUtil.getNickName
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.Click
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.AbstractItem
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.window.Window
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil
import kotlin.math.min

@Singleton
class ShopBuyInventory @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val shopService: ShopService,
    private val playerService: PlayerService,
    private val hookService: HookService
) {
    companion object {
        private val clickCooldowns = ConcurrentHashMap<UUID, Long>()
        private const val COOLDOWN_MS = 300L
    }

    fun open(player: Player, shop: Shop) {
        val gui = Gui.builder()
            .setStructure(
                "# # # # # # # # #",
                "# . . . x . . . #",
                "# # # # # # # # #"
            )
            .addIngredient('x', getBuyItem(shop))
            .build()

        val onlinePlayer = Bukkit.getPlayer(shop.ownerUuid)
        val ownerName = onlinePlayer?.getNickName(playerService)
            ?: Bukkit.getOfflinePlayer(shop.ownerUuid).name
            ?: configService.getString("config.values.unknown")

        val title = messageService.builder(configService.getString("language.inventories.shop.title"))
            .tag("owner", ownerName)
            .build()

        Window.builder()
            .setTitle(title)
            .setUpperGui(gui)
            .setViewer(player)
            .build()
            .open()
    }

    private fun getBuyItem(shop: Shop): AbstractItem {
        return object : AbstractItem() {
            override fun getItemProvider(player: Player): ItemProvider {
                val item = shop.item.clone()
                val meta = item.itemMeta

                val taxPercent = configService.getDouble("config.values.taxPercent")
                val rawLore = configService.getStringList("language.inventories.shop.itemLore")
                val formattedLore = rawLore.map { line ->
                    messageService.builder(line)
                        .forPlayer(player)
                        .tag("price", shop.price.toString())
                        .tag("stock", shop.stock.toString())
                        .tag("tax_percent", taxPercent.toString())
                        .build()
                }

                meta?.lore(formattedLore)
                item.itemMeta = meta
                return ItemWrapper(item)
            }

            override fun handleClick(clickType: ClickType, clickPlayer: Player, click: Click) {
                if (isCooldownActive(clickPlayer.uniqueId)) return
                if (!validatePreconditions(clickPlayer, shop)) return

                val banco = hookService.getHook(BancoHook::class.java).orElse(null)
                if (banco == null) {
                    sendError(clickPlayer, "language.errors.noEconomy")
                    return
                }

                val chestInv = getChestInventory(shop)
                if (chestInv == null) {
                    sendError(clickPlayer, "language.errors.shopNotAccesible")
                    return
                }

                val targetAmount = calculateTargetAmount(clickType, shop.stock)
                val maxAffordable = (getBancoBalance(banco, clickPlayer.uniqueId) / shop.price).toInt()
                val amountToBuy = min(targetAmount, maxAffordable)

                if (amountToBuy <= 0) {
                    sendErrorWithSound(clickPlayer, "language.errors.notEnoughMoney")
                    return
                }

                val sampleItem = shop.item.clone()
                val actualRemoved = removeItemsFromChest(chestInv, sampleItem, amountToBuy)
                if (actualRemoved <= 0) {
                    sendError(clickPlayer, "language.errors.errorBuyingItem")
                    return
                }

                val finalBought = deliverItemsToPlayer(clickPlayer, chestInv, shop.item, actualRemoved)
                if (finalBought <= 0) {
                    sendErrorWithSound(clickPlayer, "language.errors.inventoryFull")
                    return
                }

                val totalPrice = shop.price * finalBought
                if (!banco.withdraw(clickPlayer.uniqueId, totalPrice)) {
                    rollbackItems(clickPlayer, chestInv, shop.item, finalBought)
                    return
                }

                processFinancesAndSync(banco, shop, totalPrice, finalBought)

                notifyPurchase(clickPlayer, shop, finalBought, totalPrice)
                notifyWindows()
            }
        }
    }

    private fun isCooldownActive(uuid: UUID): Boolean {
        val now = System.currentTimeMillis()
        val lastClick = clickCooldowns[uuid] ?: 0L
        if (now - lastClick < COOLDOWN_MS) return true
        clickCooldowns[uuid] = now
        return false
    }

    private fun validatePreconditions(player: Player, shop: Shop): Boolean {
        if (player.uniqueId == shop.ownerUuid) {
            sendError(player, "language.errors.cantBuyOwnShop")
            return false
        }
        if (shop.stock <= 0) {
            sendErrorWithSound(player, "language.errors.noStock")
            return false
        }
        return true
    }

    private fun calculateTargetAmount(clickType: ClickType, currentStock: Int): Int {
        return when (clickType) {
            ClickType.LEFT -> 1
            ClickType.RIGHT -> 32
            ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> 64
            ClickType.CONTROL_DROP, ClickType.DROP, ClickType.DOUBLE_CLICK, ClickType.MIDDLE -> currentStock
            else -> 1
        }
    }

    private fun getChestInventory(shop: Shop): org.bukkit.inventory.Inventory? {
        val block = shop.location.block
        val chestBlock = block.state as? Chest
        return chestBlock?.blockInventory
    }

    private fun deliverItemsToPlayer(player: Player, chestInv: org.bukkit.inventory.Inventory, shopItem: ItemStack, amount: Int): Int {
        val itemToGive = shopItem.clone().apply { this.amount = amount }
        val leftover = player.inventory.addItem(itemToGive)

        var finalBought = amount
        if (leftover.isNotEmpty()) {
            val unhandledCount = leftover.values.sumOf { it.amount }
            finalBought -= unhandledCount

            val returnToChestItem = shopItem.clone().apply { this.amount = unhandledCount }
            chestInv.addItem(returnToChestItem)
        }
        return finalBought
    }

    private fun rollbackItems(player: Player, chestInv: org.bukkit.inventory.Inventory, shopItem: ItemStack, amount: Int) {
        val rollbackItem = shopItem.clone().apply { this.amount = amount }
        player.inventory.removeItem(rollbackItem)
        chestInv.addItem(rollbackItem)
    }

    private fun processFinancesAndSync(banco: BancoHook, shop: Shop, totalPrice: Double, finalBought: Int) {
        val taxPercent = configService.getDouble("config.values.taxPercent")
        val rawTax = totalPrice * (taxPercent / 100.0)
        val tax = ceil(rawTax)
        val ownerProfit = totalPrice - tax

        if (ownerProfit > 0) banco.deposit(shop.ownerUuid, ownerProfit)
        if (tax > 0) banco.depositToServerAccount(tax)

        shop.stock -= finalBought
        shopService.syncShop(shop)
    }

    private fun notifyPurchase(buyer: Player, shop: Shop, boughtCount: Int, totalPrice: Double) {
        val taxPercent = configService.getDouble("config.values.taxPercent")
        buyer.playSound(buyer.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)

        messageService.builder(configService.getString("language.events.onShop.buy"))
            .withPrefix()
            .tag("amount", boughtCount.toString())
            .tag("total_price", totalPrice.toString())
            .tag("tax_percent", taxPercent.toString())
            .send(buyer)

        val ownerPlayer = Bukkit.getPlayer(shop.ownerUuid)
        if (ownerPlayer != null && ownerPlayer.isOnline) {
            val rawTax = totalPrice * (taxPercent / 100.0)
            val tax = ceil(rawTax)
            val ownerProfit = totalPrice - tax

            messageService.builder(configService.getString("language.events.onShop.sold"))
                .withPrefix()
                .tag("buyer", buyer.getNickName(playerService))
                .tag("amount", boughtCount.toString())
                .tag("total_price", totalPrice.toString())
                .tag("tax", tax.toString())
                .tag("profit", ownerProfit.toString())
                .tag("item", shop.item.type.name)
                .send(ownerPlayer)

            ownerPlayer.playSound(ownerPlayer.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f)
        }
    }

    private fun sendError(player: Player, path: String) {
        messageService.builder(configService.getString(path))
            .withPrefix()
            .send(player)
    }

    private fun sendErrorWithSound(player: Player, path: String) {
        sendError(player, path)
        player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
    }

    private fun getBancoBalance(banco: BancoHook, uuid: UUID): Double {
        return try {
            val accountManager = banco.bancoInstance?.accountManager
            val account = accountManager?.getByUuid(uuid)
            account?.amount()?.toDouble() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    private fun removeItemsFromChest(chestInv: org.bukkit.inventory.Inventory, targetSample: ItemStack, amountNeeded: Int): Int {
        var remaining = amountNeeded
        val sampleOne = targetSample.clone().apply { amount = 1 }

        for (i in 0 until chestInv.size) {
            if (remaining <= 0) break
            val invItem = chestInv.getItem(i) ?: continue
            val sampleInSlot = invItem.clone().apply { amount = 1 }

            if (sampleInSlot.isSimilar(sampleOne)) {
                val toTake = min(remaining, invItem.amount)
                invItem.amount -= toTake
                remaining -= toTake

                if (invItem.amount <= 0) {
                    chestInv.setItem(i, null)
                } else {
                    chestInv.setItem(i, invItem)
                }
            }
        }

        return amountNeeded - remaining
    }
}