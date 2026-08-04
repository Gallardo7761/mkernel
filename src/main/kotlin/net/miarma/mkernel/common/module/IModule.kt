package net.miarma.mkernel.common.module

interface IModule {
    val id: String
    var isEnabled: Boolean

    fun onEnable()
    fun onDisable() {}
}