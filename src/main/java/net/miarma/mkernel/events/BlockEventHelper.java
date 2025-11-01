package net.miarma.mkernel.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlockEventHelper {
	private Player player;
    private Block block;

    private BlockEventHelper(Player player, Block block) {
        this.player = player;
        this.block = block;
    }

    public static BlockEventHelper of(Player player, Block block) {
        return new BlockEventHelper(player, block);
    }

    public void handleWheat() {
        if (block.getBlockData().getAsString().contains("age=7")) {
            int n = (int) ((Math.random() + 1) * 2.25);
            block.setBlockData(Bukkit.createBlockData("minecraft:wheat[age=0]"));
            player.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.WHEAT, n));
            player.playSound(block.getLocation(), Sound.BLOCK_GRASS_BREAK, 1, 1);
        }
    }

    public void handlePotatoes() {
        if (block.getBlockData().getAsString().contains("age=7")) {
            int n = (int) ((Math.random() + 1) * 2.25);
            block.setBlockData(Bukkit.createBlockData("minecraft:potatoes[age=0]"));
            player.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.POTATO, n));
            player.playSound(block.getLocation(), Sound.BLOCK_GRASS_BREAK, 1, 1);
        }
    }

    public void handleCarrots() {
        if (block.getBlockData().getAsString().contains("age=7")) {
            int n = (int) ((Math.random() + 1) * 2.25);
            block.setBlockData(Bukkit.createBlockData("minecraft:carrots[age=0]"));
            player.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.CARROT, n));
            player.playSound(block.getLocation(), Sound.BLOCK_GRASS_BREAK, 1, 1);
        }
    }

    public void handleBeetroots() {
        if (block.getBlockData().getAsString().contains("age=3")) {
            int n = (int) ((Math.random() + 1) * 2.75);
            block.setBlockData(Bukkit.createBlockData("minecraft:beetroots[age=0]"));
            player.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.BEETROOT, n));
            player.playSound(block.getLocation(), Sound.BLOCK_GRASS_BREAK, 1, 1);
        }
    }

    public void handleCocoa() {
        if (block.getBlockData().getAsString().contains("age=2")) {
            int n = (int) ((Math.random() + 1) * 2.25);
            block.setBlockData(Bukkit.createBlockData("minecraft:cocoa[age=0]"));
            player.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.COCOA_BEANS, n));
            player.playSound(block.getLocation(), Sound.BLOCK_GRASS_BREAK, 1, 1);
        }
    }

    public void handleTorchflower() {
        if (block.getBlockData().getAsString().contains("age=1")) {
            block.setType(Material.AIR);
            player.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.TORCHFLOWER, 1));
            player.playSound(block.getLocation(), Sound.BLOCK_GRASS_BREAK, 1, 1);
        }
    }

    public void handlePitcher() {
        if (block.getBlockData().getAsString().contains("age=4")) {
            block.setType(Material.AIR);
            player.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.PITCHER_PLANT, 1));
            player.playSound(block.getLocation(), Sound.BLOCK_GRASS_BREAK, 1, 1);
        }
    }
}
