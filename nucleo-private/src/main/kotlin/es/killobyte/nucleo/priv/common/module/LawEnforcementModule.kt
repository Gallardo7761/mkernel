package es.killobyte.nucleo.private.common.module

import com.google.inject.Inject
import com.google.inject.Singleton
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.common.IModule
import es.killobyte.nucleo.private.event.PoliceChaseListener
import es.killobyte.nucleo.private.event.TownProtectionListener
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList

@Singleton
class LawEnforcementModule @Inject constructor(
    private val plugin: NucleoPlugin,
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