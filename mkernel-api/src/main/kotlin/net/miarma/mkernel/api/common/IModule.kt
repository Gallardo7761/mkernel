package net.miarma.mkernel.api.common

interface IModule {
    val id: String
    var isEnabled: Boolean

    fun onEnable()
    fun onDisable() {}

    fun isFeatureEnabled(configService: IConfigService, feature: String): Boolean {
        if (!isEnabled) return false
        return configService.getBoolean("modules.$id.$feature", true)
    }
}