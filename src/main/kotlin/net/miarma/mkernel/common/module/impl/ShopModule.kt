package net.miarma.mkernel.common.module.impl

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.module.IModule
import net.miarma.mkernel.event.ShopListener
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList

@Singleton
class ShopModule @Inject constructor(
    private val plugin: MKernel,
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