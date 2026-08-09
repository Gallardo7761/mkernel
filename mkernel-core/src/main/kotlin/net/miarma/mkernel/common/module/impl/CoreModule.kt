package net.miarma.mkernel.common.module.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.common.IModule
import net.miarma.mkernel.event.StartupCommandsListener
import org.bukkit.Bukkit

@Singleton
class CoreModule @Inject constructor(
    private val plugin: MKernel,
    private val startupCommandsListener: StartupCommandsListener
) : IModule {
    override val id = "core"
    override var isEnabled = false

    override fun onEnable() {
        if (isEnabled) return
        Bukkit.getPluginManager().registerEvents(startupCommandsListener, plugin)
        isEnabled = true
    }

    override fun onDisable() {
        if (!isEnabled) return
        isEnabled = false
    }
}