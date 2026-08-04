package net.miarma.mkernel.common.module.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.module.IModule

@Singleton
class TeleportModule @Inject constructor() : IModule {

    override val id = "teleport"
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