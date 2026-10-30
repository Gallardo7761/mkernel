package es.killobyte.nucleo.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.annotation.LoaderPriority
import es.killobyte.nucleo.api.common.IHook
import es.killobyte.nucleo.api.common.IService
import java.util.*

@Singleton
@LoaderPriority(LoaderPriority.HIGH)
class HookService @Inject constructor(
    private val plugin: NucleoPlugin
) : IService {

    private val hooks = mutableMapOf<Class<out IHook>, IHook>()

    override fun onDisable() {
        disableHooks()
    }

    fun registerHooks(vararg hooksToRegister: IHook) {
        for (hook in hooksToRegister) {
            if (hook.isInstalled()) {
                try {
                    hook.register()
                    hooks[hook.javaClass] = hook
                    plugin.logger.info("Hook loaded: ${hook.pluginName}")
                } catch (e: Exception) {
                    plugin.logger.severe("Error initializing hook ${hook.pluginName}")
                    e.printStackTrace()
                }
            } else {
                plugin.logger.info("Plugin ${hook.pluginName} not found. Skipping hook.")
            }
        }
    }

    fun disableHooks() {
        hooks.values.forEach { it.unregister() }
        hooks.clear()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : IHook> getHook(hookClass: Class<T>): Optional<T> {
        return Optional.ofNullable(hooks[hookClass] as? T)
    }

    fun isLoaded(hookClass: Class<out IHook>): Boolean {
        return hooks.containsKey(hookClass)
    }
}
