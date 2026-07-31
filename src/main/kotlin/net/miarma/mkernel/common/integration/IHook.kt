package net.miarma.mkernel.common.integration

import org.bukkit.Bukkit

interface IHook {
    val pluginName: String

    fun register()

    fun unregister() {}

    fun isInstalled(): Boolean {
        val name = pluginName
        return Bukkit.getPluginManager().getPlugin(name) != null &&
                Bukkit.getPluginManager().isPluginEnabled(name)
    }
}
