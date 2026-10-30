package es.killobyte.nucleo.priv.common.module

import com.google.inject.Inject
import com.google.inject.Singleton
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.common.IModule
import es.killobyte.nucleo.priv.event.BlindGodListener
import org.bukkit.Bukkit

@Singleton
class BlindGodModule @Inject constructor(
    private val plugin: NucleoPlugin,
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