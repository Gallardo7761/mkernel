package net.miarma.mkernel.common.minecraft.inventories;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class InvseeInventory {

    public static final Map<Player, Player> adminPlayerMap = new HashMap<>();
    public static final Map<Player, Inventory> activeInvseeInventories = new HashMap<>();

    public static void setInvForAdmin(Player admin, Inventory inv, Player target) {
        adminPlayerMap.put(admin, target);
        activeInvseeInventories.put(admin, inv);
    }

    public static Inventory getInvForAdmin(Player admin) {
        return activeInvseeInventories.get(admin);
    }

    public static boolean isInvseeInventory(Inventory inv) {
        return activeInvseeInventories.containsValue(inv);
    }

    public static void removeInv(Player admin) {
        adminPlayerMap.remove(admin);
        activeInvseeInventories.remove(admin);
    }
}
