package net.miarma.mkernel.util;

import net.miarma.mkernel.common.minecraft.MinepacksAccessor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {
    public static void refillItem(Player player, Material material, EquipmentSlot hand) {
        ItemStack[] items = player.getInventory().getStorageContents();

        for (int i = 0; i < 36; ++i) {
            if (items[i] != null && isValidSlot(i, player) && items[i].getType().equals(material)) {
                if (hand.equals(EquipmentSlot.HAND)) {
                    player.getInventory().setItemInMainHand(items[i]);
                    player.getInventory().setItem(i, null);
                    break;
                }

                if (hand.equals(EquipmentSlot.OFF_HAND)) {
                    player.getInventory().setItemInOffHand(items[i]);
                    player.getInventory().setItem(i, null);
                    break;
                }
            }
        }

    }

    public static void refillItemFromMinepack(Player player, Material material, EquipmentSlot hand) {
        Inventory backpack = MinepacksAccessor.getPlayerBackpackInventory(player);

        if (backpack != null) {
            ItemStack[] contents = backpack.getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack itemStack = contents[i];
                if (itemStack != null && isValidSlot(i, player) && itemStack.getType() == material) {
                    if (hand == EquipmentSlot.HAND) {
                        player.getInventory().setItemInMainHand(itemStack);
                    } else if (hand == EquipmentSlot.OFF_HAND) {
                        player.getInventory().setItemInOffHand(itemStack);
                    }
                    contents[i] = null;
                    backpack.setContents(contents);
                    break;
                }
            }
        }
    }

    public static boolean isValidSlot(int i, Player player) {
        return i != player.getInventory().getHeldItemSlot() && i != 40;
    }

    public static int getItemCount(Inventory inventory, Material material) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }
}
