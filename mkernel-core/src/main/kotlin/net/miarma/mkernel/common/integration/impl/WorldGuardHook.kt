package net.miarma.mkernel.common.integration.impl

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException
import com.sk89q.worldguard.protection.regions.RegionContainer
import net.miarma.mkernel.api.common.IHook
import net.miarma.mkernel.common.config.ConfigKeys
import org.bukkit.Location
import org.bukkit.entity.Player

class WorldGuardHook : IHook {
    override val pluginName = "WorldGuard"

    companion object {
        lateinit var CAN_CREATE_SHOP_FLAG: StateFlag
            private set

        fun registerFlag() {
            val registry = WorldGuard.getInstance().flagRegistry
            try {
                val flag = StateFlag("can-create-shop", true)
                registry.register(flag)
                CAN_CREATE_SHOP_FLAG = flag
            } catch (e: FlagConflictException) {
                val existing = registry.get("can-create-shop")
                if (existing is StateFlag) {
                    CAN_CREATE_SHOP_FLAG = existing
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun register() {

    }

    fun canCreateShop(player: Player, location: Location): Boolean {
        if (!isInstalled()) return true
        if (player.hasPermission(ConfigKeys.Settings.Shops.PERM_ADMIN)) return true

        val localPlayer = com.sk89q.worldguard.bukkit.WorldGuardPlugin.inst().wrapPlayer(player)
        val container: RegionContainer = WorldGuard.getInstance().platform.regionContainer
        val query = container.createQuery()

        val loc = BukkitAdapter.adapt(location)
        return query.testState(loc, localPlayer, CAN_CREATE_SHOP_FLAG)
    }
}