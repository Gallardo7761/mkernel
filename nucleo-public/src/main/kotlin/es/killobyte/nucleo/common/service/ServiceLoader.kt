package es.killobyte.nucleo.common.service

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Singleton
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.annotation.LoaderPriority
import es.killobyte.nucleo.api.common.IService
import org.reflections.Reflections
import java.lang.reflect.Modifier

@Singleton
class ServiceLoader @Inject constructor(
    private val plugin: NucleoPlugin,
    private val injector: Injector,
    private val reflections: Reflections
) {
    private val services = mutableListOf<IService>()

    fun loadAll() {
        val serviceClasses = reflections.getSubTypesOf(IService::class.java)

        val instances = serviceClasses
            .filter { !it.isInterface && !Modifier.isAbstract(it.modifiers) }
            .mapNotNull { clazz ->
                runCatching { injector.getInstance(clazz) }.getOrElse { e ->
                    plugin.logger.severe("Error instantiating service ${clazz.simpleName}: ${e.message}")
                    null
                }
            }

        val sortedServices = instances.sortedBy { service ->
            val annotation = service.javaClass.getAnnotation(LoaderPriority::class.java)
            annotation?.priority ?: LoaderPriority.NORMAL
        }

        for (service in sortedServices) {
            runCatching {
                service.onEnable()
                services.add(service)
            }.onFailure { e ->
                plugin.logger.severe("Error starting service ${service.javaClass.simpleName}: ${e.message}")
                e.printStackTrace()
            }
        }

        plugin.logger.info("Loaded ${services.size} services!")
    }

    fun disableAll() {
        services.reversed().forEach { runCatching { it.onDisable() } }
        services.clear()
    }
}