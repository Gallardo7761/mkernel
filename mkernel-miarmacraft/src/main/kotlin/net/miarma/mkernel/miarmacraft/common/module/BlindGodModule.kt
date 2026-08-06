package net.miarma.mkernel.miarmacraft.common.module

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.common.IModule
import net.miarma.mkernel.miarmacraft.event.BlindGodListener
import org.bukkit.Bukkit

@Singleton
class BlindGodModule @Inject constructor(
    private val plugin: MKernel,
    private val blindGodListener: BlindGodListener
) : IModule {

    override val id = "blindGod"
    override var isEnabled = false

    override fun onEnable() {
        if (isEnabled) return
        Bukkit.getPluginManager().registerEvents(blindGodListener, plugin)
        isEnabled = true
    }

    override fun onDisable() {
        if (!isEnabled) return
        isEnabled = false
    }
}