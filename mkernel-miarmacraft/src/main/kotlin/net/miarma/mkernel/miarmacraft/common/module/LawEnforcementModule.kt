package net.miarma.mkernel.miarmacraft.common.module

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.common.IModule
import net.miarma.mkernel.miarmacraft.event.LawEnforcementListener
import org.bukkit.Bukkit

@Singleton
class LawEnforcementModule @Inject constructor(
    private val plugin: MKernel,
    private val lawEnforcementListener: LawEnforcementListener
) : IModule {

    override val id = "lawEnforcement"
    override var isEnabled = false

    override fun onEnable() {
        if (isEnabled) return
        Bukkit.getPluginManager().registerEvents(lawEnforcementListener, plugin)
        isEnabled = true
    }

    override fun onDisable() {
        if (!isEnabled) return
        isEnabled = false
    }
}