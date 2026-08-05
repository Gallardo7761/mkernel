package net.miarma.mkernel.common.module

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Singleton
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.common.IModule
import net.miarma.mkernel.common.service.impl.ConfigService
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.reflections.Reflections
import java.lang.reflect.Modifier

@Singleton
class ModuleLoader @Inject constructor(
    private val injector: Injector,
    private val configService: ConfigService
) {
    private val allModules = mutableMapOf<String, IModule>()

    fun loadAll() {
        val reflections = Reflections("net.miarma.mkernel")
        val moduleClasses = reflections.getSubTypesOf(IModule::class.java)

        var loadedCount = 0
        for (clazz in moduleClasses) {
            if (clazz.isInterface || Modifier.isAbstract(clazz.modifiers)) continue

            runCatching {
                val instance = injector.getInstance(clazz)
                allModules[instance.id] = instance

                val isEnabledInConfig = configService.getBoolean("modules.${instance.id}.enabled", true)
                if (isEnabledInConfig) {
                    enableModule(instance)
                    loadedCount++
                }
            }.onFailure { e ->
                MKernel.LOGGER.severe("Error instantiating module ${clazz.simpleName}: ${e.message}")
                e.printStackTrace()
            }
        }
        MKernel.LOGGER.info("Loaded $loadedCount modules!")
    }

    fun isModuleEnabled(id: String): Boolean {
        return allModules[id]?.isEnabled
            ?: (configService.getBoolean("modules.$id.enabled", true) || configService.getBoolean("modules.$id", true))
    }

    fun toggleModule(id: String, enable: Boolean) {
        val module = allModules[id] ?: return
        if (enable) {
            enableModule(module)
        } else {
            disableModule(module)
        }
        configService.set("modules.$id", enable)
    }

    private fun enableModule(module: IModule) {
        if (module.isEnabled) return
        runCatching {
            module.onEnable()
            module.isEnabled = true

            if (module is Listener) {
                Bukkit.getPluginManager().registerEvents(module, MKernel.PLUGIN)
            }
            MKernel.LOGGER.info("Enabled module: ${module.id}")
        }.onFailure { e ->
            MKernel.LOGGER.severe("Error enabling module ${module.id}: ${e.message}")
        }
    }

    private fun disableModule(module: IModule) {
        if (!module.isEnabled) return
        runCatching {
            module.onDisable()
            module.isEnabled = false

            if (module is Listener) {
                HandlerList.unregisterAll(module)
            }
            MKernel.LOGGER.info("Disabled module: ${module.id}")
        }.onFailure { e ->
            MKernel.LOGGER.severe("Error disabling module ${module.id}: ${e.message}")
        }
    }

    fun disableAll() {
        allModules.values.forEach { disableModule(it) }
        allModules.clear()
    }
}