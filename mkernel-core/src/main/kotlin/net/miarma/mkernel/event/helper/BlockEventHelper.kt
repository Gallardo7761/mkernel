package net.miarma.mkernel.event.helper

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

enum class HarvestableCrop(
    val blockMaterial: Material,
    val maxAge: Int,
    val resetData: String?,
    val dropMaterial: Material,
    val multiplier: Double?
) {
    WHEAT(Material.WHEAT, 7, "minecraft:wheat[age=0]", Material.WHEAT, 2.25),
    POTATOES(Material.POTATOES, 7, "minecraft:potatoes[age=0]", Material.POTATO, 2.25),
    CARROTS(Material.CARROTS, 7, "minecraft:carrots[age=0]", Material.CARROT, 2.25),
    BEETROOTS(Material.BEETROOTS, 3, "minecraft:beetroots[age=0]", Material.BEETROOT, 2.75),
    COCOA(Material.COCOA, 2, "minecraft:cocoa[age=0]", Material.COCOA_BEANS, 2.25),
    TORCHFLOWER(Material.TORCHFLOWER_CROP, 1, null, Material.TORCHFLOWER, null),
    PITCHER(Material.PITCHER_CROP, 4, null, Material.PITCHER_PLANT, null);

    companion object {
        fun from(material: Material) = entries.find { it.blockMaterial == material }
    }
}

class BlockEventHelper private constructor(
    private val player: Player,
    private val block: Block
) {
    companion object {
        fun of(player: Player, block: Block) = BlockEventHelper(player, block)
    }

    fun handleCrop(crop: HarvestableCrop) {
        if (block.blockData.asString.contains("age=${crop.maxAge}")) {
            val amount = if (crop.multiplier != null) {
                ((Math.random() + 1) * crop.multiplier).toInt()
            } else 1

            if (crop.resetData != null) {
                block.blockData = Bukkit.createBlockData(crop.resetData)
            } else {
                block.type = Material.AIR
            }

            player.world.dropItemNaturally(block.location, ItemStack(crop.dropMaterial, amount))
            player.playSound(block.location, Sound.BLOCK_GRASS_BREAK, 1f, 1f)
        }
    }
}