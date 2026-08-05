package net.miarma.mkernel.common.module.impl

import com.google.inject.Singleton
import net.miarma.mkernel.api.common.IModule

@Singleton
class AdminModule : IModule {
    override val id = "admin"
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