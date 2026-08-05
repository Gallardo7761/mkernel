package net.miarma.mkernel.event.helper

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BlockEventHelper private constructor(
    private val player: Player,
    private val block: Block
) {
    companion object {
        fun of(player: Player, block: Block) = BlockEventHelper(player, block)
    }

    fun handleWheat() {
        if (block.blockData.getAsString().contains("age=7")) {
            val n = ((Math.random() + 1) * 2.25).toInt()
            block.blockData = Bukkit.createBlockData("minecraft:wheat[age=0]")
            player.world.dropItemNaturally(block.location, ItemStack(Material.WHEAT, n))
            player.playSound(block.location, Sound.BLOCK_GRASS_BREAK, 1f, 1f)
        }
    }

    fun handlePotatoes() {
        if (block.blockData.getAsString().contains("age=7")) {
            val n = ((Math.random() + 1) * 2.25).toInt()
            block.blockData = Bukkit.createBlockData("minecraft:potatoes[age=0]")
            player.world.dropItemNaturally(block.location, ItemStack(Material.POTATO, n))
            player.playSound(block.location, Sound.BLOCK_GRASS_BREAK, 1f, 1f)
        }
    }

    fun handleCarrots() {
        if (block.blockData.getAsString().contains("age=7")) {
            val n = ((Math.random() + 1) * 2.25).toInt()
            block.blockData = Bukkit.createBlockData("minecraft:carrots[age=0]")
            player.world.dropItemNaturally(block.location, ItemStack(Material.CARROT, n))
            player.playSound(block.location, Sound.BLOCK_GRASS_BREAK, 1f, 1f)
        }
    }

    fun handleBeetroots() {
        if (block.blockData.getAsString().contains("age=3")) {
            val n = ((Math.random() + 1) * 2.75).toInt()
            block.blockData = Bukkit.createBlockData("minecraft:beetroots[age=0]")
            player.world.dropItemNaturally(block.location, ItemStack(Material.BEETROOT, n))
            player.playSound(block.location, Sound.BLOCK_GRASS_BREAK, 1f, 1f)
        }
    }

    fun handleCocoa() {
        if (block.blockData.getAsString().contains("age=2")) {
            val n = ((Math.random() + 1) * 2.25).toInt()
            block.blockData = Bukkit.createBlockData("minecraft:cocoa[age=0]")
            player.world.dropItemNaturally(block.location, ItemStack(Material.COCOA_BEANS, n))
            player.playSound(block.location, Sound.BLOCK_GRASS_BREAK, 1f, 1f)
        }
    }

    fun handleTorchflower() {
        if (block.blockData.getAsString().contains("age=1")) {
            block.type = Material.AIR
            player.world.dropItemNaturally(block.location, ItemStack(Material.TORCHFLOWER, 1))
            player.playSound(block.location, Sound.BLOCK_GRASS_BREAK, 1f, 1f)
        }
    }

    fun handlePitcher() {
        if (block.blockData.getAsString().contains("age=4")) {
            block.type = Material.AIR
            player.world.dropItemNaturally(block.location, ItemStack(Material.PITCHER_PLANT, 1))
            player.playSound(block.location, Sound.BLOCK_GRASS_BREAK, 1f, 1f)
        }
    }
}
