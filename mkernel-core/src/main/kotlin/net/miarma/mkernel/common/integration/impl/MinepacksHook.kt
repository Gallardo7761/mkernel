package net.miarma.mkernel.common.integration.impl

import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksPlugin
import net.miarma.mkernel.api.common.IHook
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class MinepacksHook : IHook {
    override val pluginName = "Minepacks"
    private var minepacksPlugin: MinepacksPlugin? = null

    override fun register() {
        minepacksPlugin = Bukkit.getPluginManager().getPlugin("Minepacks") as? MinepacksPlugin
    }

    fun getPlayerBackpackInventory(player: Player): Inventory? {
        val bp = minepacksPlugin?.getBackpackCachedOnly(player)
        return bp?.inventory
    }
}
