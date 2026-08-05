package net.miarma.mkernel.common.module.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.common.IModule
import net.miarma.mkernel.event.ChatListener
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList

@Singleton
class ChatModule @Inject constructor(
    private val plugin: MKernel,
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