package es.killobyte.nucleo.common.integration

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Singleton
import es.killobyte.nucleo.api.common.IHook
import es.killobyte.nucleo.common.service.impl.HookService
import org.reflections.Reflections

@Singleton
class HookLoader @Inject constructor(
    private val injector: Injector,
    private val hookService: HookService,
    private val reflections: Reflections
) {
    fun loadAll() {
        val hookClasses = reflections.getSubTypesOf(IHook::class.java)

        val hooks = hookClasses.mapNotNull { clazz ->
            runCatching { injector.getInstance(clazz) }.getOrNull()
        }.toTypedArray()

        hookService.registerHooks(*hooks)
    }
}