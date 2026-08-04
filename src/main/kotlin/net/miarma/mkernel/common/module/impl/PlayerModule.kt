package net.miarma.mkernel.common.module.impl

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.module.IModule
import net.miarma.mkernel.event.PlayerAdvancementListener
import net.miarma.mkernel.event.PlayerConnectionListener
import net.miarma.mkernel.event.PlayerStatusListener
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

@Singleton
class PlayerModule @Inject constructor(
    private val plugin: MKernel,
    private val playerAdvancementListener: PlayerAdvancementListener,
    private val playerConnectionListener: PlayerConnectionListener,
    private val playerStatusListener: PlayerStatusListener
) : IModule {

    override val id = "player"
    override var isEnabled = false

    private val listeners: List<Listener> = listOf(
        playerAdvancementListener,
        playerConnectionListener,
        playerStatusListener
    )

    override fun onEnable() {
        if (isEnabled) return
        listeners.forEach { Bukkit.getPluginManager().registerEvents(it, plugin) }
        isEnabled = true
    }

    override fun onDisable() {
        if (!isEnabled) return
        listeners.forEach { HandlerList.unregisterAll(it) }
        isEnabled = false
    }
}