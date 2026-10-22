package net.miarma.mkernel.miarmacraft.common.module

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.api.common.IModule

@Singleton
class PaysheetModule @Inject constructor() : IModule {

    override val id = "paysheet"
    override var isEnabled = false

    override fun onEnable() {
        if (isEnabled) return
        isEnabled = true
    }

    override fun onDisable() {
        if (!isEnabled) return
        isEnabled = false
    }
}