package net.miarma.mkernel.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.integration.impl.MinepacksHook
import net.miarma.mkernel.common.service.IService
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

@Singleton
class InventoryService @Inject constructor(private val hookService: HookService) : IService {

    fun refillItem(player: Player, material: Material, hand: EquipmentSlot) {
        val items = player.inventory.storageContents
        for (i in items.indices) {
            if (items[i] != null && isValidSlot(i, player) && items[i]?.type == material) {
                when (hand) {
                    EquipmentSlot.HAND -> player.inventory.setItemInMainHand(items[i])
                    EquipmentSlot.OFF_HAND -> player.inventory.setItemInOffHand(items[i])
                    else -> {}
                }
                player.inventory.setItem(i, null)
                break
            }
        }
    }

    fun refillItemFromMinepack(player: Player, material: Material, hand: EquipmentSlot) {
        hookService.getHook(MinepacksHook::class.java).ifPresent { hook ->
            val backpack = hook.getPlayerBackpackInventory(player) ?: return@ifPresent
            val contents = backpack.contents
            for (i in contents.indices) {
                val itemStack = contents[i]
                if (itemStack != null && isValidSlot(i, player) && itemStack.type == material) {
                    when (hand) {
                        EquipmentSlot.HAND -> player.inventory.setItemInMainHand(itemStack)
                        EquipmentSlot.OFF_HAND -> player.inventory.setItemInOffHand(itemStack)
                        else -> {}
                    }
                    contents[i] = null
                    backpack.contents = contents
                    break
                }
            }
        }
    }

    fun isValidSlot(i: Int, player: Player): Boolean {
        return i != player.inventory.heldItemSlot && i != 40
    }

    fun getItemCount(inventory: org.bukkit.inventory.Inventory, material: Material): Int {
        return inventory.contents.sumOf { it?.takeIf { it.type == material }?.amount ?: 0 }
    }

    companion object {
        fun toBase64(items: Array<ItemStack?>?): ByteArray {
            return items?.let { ItemStack.serializeItemsAsBytes(it.toList()) } ?: byteArrayOf()
        }

        @Suppress("UNCHECKED_CAST")
        fun fromBase64(bytes: ByteArray?): Array<ItemStack?> {
            return bytes?.let { ItemStack.deserializeItemsFromBytes(it) as Array<ItemStack?>? } ?: emptyArray()
        }
    }
}
