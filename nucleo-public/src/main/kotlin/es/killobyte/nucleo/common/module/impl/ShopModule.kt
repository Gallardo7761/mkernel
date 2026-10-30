package es.killobyte.nucleo.common.module.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.common.IModule
import es.killobyte.nucleo.event.ShopListener
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList

@Singleton
class ShopModule @Inject constructor(
    private val plugin: NucleoPlugin,
    private val shopListener: ShopListener
) : IModule {

    override val id = "shop"
    override var isEnabled = false

    override fun onEnable() {
        if (isEnabled) return
        Bukkit.getPluginManager().registerEvents(shopListener, plugin)
        isEnabled = true
    }

    override fun onDisable() {
        if (!isEnabled) return
        HandlerList.unregisterAll(shopListener)
        isEnabled = false
    }
}