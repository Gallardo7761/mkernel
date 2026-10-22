package net.miarma.mkernel.miarmacraft.common.module

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.common.IModule
import net.miarma.mkernel.miarmacraft.event.PoliceChaseListener
import net.miarma.mkernel.miarmacraft.event.TownProtectionListener
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList

@Singleton
class LawEnforcementModule @Inject constructor(
    private val plugin: MKernel,
    private val townProtectionListener: TownProtectionListener,
    private val policeChaseListener: PoliceChaseListener
) : IModule {

    override val id = "lawEnforcement"
    override var isEnabled = false

    override fun onEnable() {
        if (isEnabled) return
        Bukkit.getPluginManager().registerEvents(townProtectionListener, plugin)
        Bukkit.getPluginManager().registerEvents(policeChaseListener, plugin) // NUEVO
        isEnabled = true
    }

    override fun onDisable() {
        if (!isEnabled) return
        HandlerList.unregisterAll(townProtectionListener)
        HandlerList.unregisterAll(policeChaseListener) // NUEVO
        isEnabled = false
    }
}