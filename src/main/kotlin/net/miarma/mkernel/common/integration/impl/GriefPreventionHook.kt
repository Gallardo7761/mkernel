package net.miarma.mkernel.common.integration.impl

import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.miarma.mkernel.common.integration.IHook
import org.bukkit.Location
import org.bukkit.entity.Player

class GriefPreventionHook : IHook {

    override val pluginName = "GriefPrevention"
    private var gpInstance: GriefPrevention? = null

    override fun register() {
        gpInstance = GriefPrevention.instance
    }

    fun hasAccess(player: Player, location: Location): Boolean {
        val gp = gpInstance ?: return true
        val claim = gp.dataStore?.getClaimAt(location, true, null) ?: return true
        return claim.checkPermission(player, me.ryanhamshire.GriefPrevention.ClaimPermission.Access, null) == null
    }
}
