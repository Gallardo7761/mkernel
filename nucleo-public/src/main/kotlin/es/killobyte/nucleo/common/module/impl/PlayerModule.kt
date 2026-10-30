package es.killobyte.nucleo.common.module.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.common.IModule
import es.killobyte.nucleo.event.PlayerAdvancementListener
import es.killobyte.nucleo.event.PlayerConnectionListener
import es.killobyte.nucleo.event.PlayerStatusListener
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

@Singleton
class PlayerModule @Inject constructor(
    private val plugin: NucleoPlugin,
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