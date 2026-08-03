package net.miarma.mkernel.common.service

import MKernel
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Singleton
import net.miarma.mkernel.common.annotation.LoaderPriority
import org.reflections.Reflections
import java.lang.reflect.Modifier

@Singleton
class ServiceLoader @Inject constructor(
    private val injector: Injector
) {
    private val services = mutableListOf<IService>()

    fun loadAll() {
        val reflections = Reflections("net.miarma.mkernel.common.service.impl")
        val serviceClasses = reflections.getSubTypesOf(IService::class.java)

        val instances = serviceClasses
            .filter { !it.isInterface && !Modifier.isAbstract(it.modifiers) }
            .mapNotNull { clazz ->
                runCatching { injector.getInstance(clazz) }.getOrElse { e ->
                    MKernel.LOGGER.severe("Error instantiating service ${clazz.simpleName}: ${e.message}")
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
                MKernel.LOGGER.severe("Error starting service ${service.javaClass.simpleName}: ${e.message}")
                e.printStackTrace()
            }
        }

        MKernel.LOGGER.info("Loaded ${services.size} services!")
    }

    fun disableAll() {
        services.reversed().forEach { runCatching { it.onDisable() } }
        services.clear()
    }
}