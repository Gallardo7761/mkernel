package net.miarma.mkernel.common.module

import net.miarma.mkernel.common.service.impl.ConfigService

interface IModule {
    val id: String
    var isEnabled: Boolean

    fun onEnable()
    fun onDisable() {}

    fun isFeatureEnabled(configService: ConfigService, feature: String): Boolean {
        if (!isEnabled) return false
        return configService.getBoolean("modules.$id.$feature", true)
    }
}