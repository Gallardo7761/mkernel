package net.miarma.mkernel.listeners;

import de.tr7zw.nbtapi.NBTItem;
import net.miarma.mkernel.common.annotation.Version;
import net.miarma.mkernel.common.minecraft.MinepackAccessor;
import net.miarma.mkernel.config.providers.ConfigProvider;
import net.miarma.mkernel.util.InventoryUtil;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;

import static net.miarma.mkernel.common.Constants.ADMIN_STICK_KEY;
import static net.miarma.mkernel.common.Constants.SPECIAL_ITEM_TAG;

public class ItemListener implements Listener {
    
    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent event) {    	
        if (!ConfigProvider.Modules.isAutoItemRefillEnabled()) return;

        Player player = event.getPlayer();
        ItemStack brokenItem = event.getBrokenItem();
        Material brokenType = brokenItem.getType();

        PlayerInventory inventory = player.getInventory();
        ItemStack mainHandItem = inventory.getItemInMainHand();

        EquipmentSlot hand = (mainHandItem.getType() == brokenType) ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;

        if (brokenItem.getAmount() > 1) return;

        int countInInventory = InventoryUtil.getItemCount(inventory, brokenType);
        if (countInInventory > 1) {
            InventoryUtil.refillItem(player, brokenType, hand);
            return;
        }

        if (!MinepackAccessor.isLoaded()) return;

        Inventory backpack = MinepackAccessor.getPlayerBackpackInventory(player);
        if (backpack == null || backpack.isEmpty()) return;

        boolean foundInBackpack = Arrays.stream(backpack.getContents())
                .anyMatch(item -> item != null && item.getType() == brokenType);

        if (foundInBackpack) {
            InventoryUtil.refillItemFromMinepack(player, brokenType, hand);
        }
    }

    
    @EventHandler
    public void onAdminStickAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.STICK) return;

        NBTItem nbt = new NBTItem(item);
        String specialTag = nbt.getString(SPECIAL_ITEM_TAG);
        if (!ADMIN_STICK_KEY.equals(specialTag)) return;

        if (event.getEntity() instanceof LivingEntity target) {
            target.setHealth(0.0);
        }
    }


}
