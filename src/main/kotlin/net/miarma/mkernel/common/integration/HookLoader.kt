package net.miarma.mkernel.common.integration

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Singleton
import net.miarma.mkernel.common.service.impl.HookService
import org.reflections.Reflections

@Singleton
class HookLoader @Inject constructor(
    private val injector: Injector,
    private val hookService: HookService
) {
    fun loadAll() {
        val reflections = Reflections("net.miarma.mkernel.common.integration.impl")
        val hookClasses = reflections.getSubTypesOf(IHook::class.java)

        val hooks = hookClasses.mapNotNull { clazz ->
            runCatching { injector.getInstance(clazz) }.getOrNull()
        }.toTypedArray()

        hookService.registerHooks(*hooks)
    }
}