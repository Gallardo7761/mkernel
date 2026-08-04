package net.miarma.mkernel.common.module.impl

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.module.IModule
import net.miarma.mkernel.event.WorldInteractionListener
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList

@Singleton
class WorldModule @Inject constructor(
    private val plugin: MKernel,
    private val worldInteractionListener: WorldInteractionListener
) : IModule {

    override val id = "world"
    override var isEnabled = false

    override fun onEnable() {
        if (isEnabled) return
        Bukkit.getPluginManager().registerEvents(worldInteractionListener, plugin)
        isEnabled = true
    }

    override fun onDisable() {
        if (!isEnabled) return
        HandlerList.unregisterAll(worldInteractionListener)
        isEnabled = false
    }
}