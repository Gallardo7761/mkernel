package es.killobyte.nucleo.common.module.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.common.IModule
import es.killobyte.nucleo.event.ChatListener
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList

@Singleton
class ChatModule @Inject constructor(
    private val plugin: NucleoPlugin,
    private val chatListener: ChatListener
) : IModule {

    override val id = "chat"
    override var isEnabled = false

    override fun onEnable() {
        if (isEnabled) return
        Bukkit.getPluginManager().registerEvents(chatListener, plugin)
        isEnabled = true
    }

    override fun onDisable() {
        if (!isEnabled) return
        HandlerList.unregisterAll(chatListener)
        isEnabled = false
    }
}