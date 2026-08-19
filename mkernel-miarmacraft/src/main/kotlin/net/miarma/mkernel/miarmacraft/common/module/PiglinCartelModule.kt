package net.miarma.mkernel.miarmacraft.common.module

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.common.IModule
import net.miarma.mkernel.miarmacraft.event.PiglinCartelListener
import org.bukkit.Bukkit

@Singleton
class PiglinCartelModule @Inject constructor(
    private val plugin: MKernel,
    private val piglinCartelListener: PiglinCartelListener
) : IModule {

    override val id = "piglinCartel"
    override var isEnabled = false

    override fun onEnable() {
        if (isEnabled) return
        Bukkit.getPluginManager().registerEvents(piglinCartelListener, plugin)
        isEnabled = true
    }

    override fun onDisable() {
        if (!isEnabled) return
        isEnabled = false
    }
}