package net.miarma.mkernel.common.service.impl

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.integration.IHook
import net.miarma.mkernel.common.service.IService
import java.util.*

@Singleton
class HookService @Inject constructor() : IService {

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
                    MKernel.LOGGER.info("Hook loaded: ${hook.pluginName}")
                } catch (e: Exception) {
                    MKernel.LOGGER.severe("Error initializing hook ${hook.pluginName}")
                    e.printStackTrace()
                }
            } else {
                MKernel.LOGGER.info("Plugin ${hook.pluginName} not found. Skipping hook.")
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
