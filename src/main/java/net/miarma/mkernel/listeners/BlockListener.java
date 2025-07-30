package net.miarma.mkernel.listeners;

import de.tr7zw.nbtapi.NBTItem;
import net.miarma.mkernel.common.annotation.Version;
import net.miarma.mkernel.common.minecraft.MinepackAccessor;
import net.miarma.mkernel.config.providers.ConfigProvider;
import net.miarma.mkernel.util.InventoryUtil;
import net.miarma.mkernel.util.ItemUtil;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.Arrays;

import static net.miarma.mkernel.common.Constants.SPAWNER_BREAKER_KEY;
import static net.miarma.mkernel.common.Constants.SPECIAL_ITEM_TAG;

public class BlockListener implements Listener {
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {    	
        if (!ConfigProvider.Modules.isAutoItemRefillEnabled()) return;

        Player player = event.getPlayer();
        Material placedType = event.getBlockPlaced().getType();
        ItemStack itemInHand = event.getItemInHand();

        if (ItemUtil.isAxe(itemInHand)) return;
        if (itemInHand.getAmount() > 1) return;

        Inventory inv = player.getInventory();
        int countInInv = InventoryUtil.getItemCount(inv, placedType);

        if (countInInv > 1) {
            InventoryUtil.refillItem(player, placedType, event.getHand());
            return;
        }

        if (!MinepackAccessor.isLoaded()) return;

        Inventory backpack = MinepackAccessor.getPlayerBackpackInventory(player);
        if (backpack == null || backpack.isEmpty()) return;

        boolean hasMatchInBackpack = Arrays.stream(backpack.getContents())
                .anyMatch(item -> item != null && item.getType() == placedType);

        if (hasMatchInBackpack) {
            InventoryUtil.refillItemFromMinepack(player, placedType, event.getHand());
        }
    }

    
    @EventHandler
    public void onSpawnerBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand == null || itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0)
            return;

        NBTItem nbtItem = new NBTItem(itemInHand);
        String specialType = nbtItem.getString(SPECIAL_ITEM_TAG);

        if (!SPAWNER_BREAKER_KEY.equals(specialType))
            return;

        int probability = ConfigProvider.Values.getSpawnerBreakerProbability();
        int roll = (int) (Math.random() * 100);

        if (roll > probability) {
            event.setCancelled(true);
            player.playSound(player, Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            player.getInventory().setItemInMainHand(null);
            return;
        }

        Block block = event.getBlock();
        if (!(block.getState() instanceof CreatureSpawner)) return;

        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        ItemStack droppedSpawner = new ItemStack(Material.SPAWNER);
        BlockStateMeta meta = (BlockStateMeta) droppedSpawner.getItemMeta();

        if (meta != null) {
            meta.setBlockState(spawner);
            droppedSpawner.setItemMeta(meta);
        }

        block.getWorld().dropItemNaturally(block.getLocation(), droppedSpawner);
        block.setType(Material.AIR);
    }

}
