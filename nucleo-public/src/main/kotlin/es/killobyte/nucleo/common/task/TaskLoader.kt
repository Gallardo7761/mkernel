package es.killobyte.nucleo.common.task

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Singleton
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.annotation.RequiresModule
import es.killobyte.nucleo.api.common.ITask
import es.killobyte.nucleo.common.module.ModuleLoader
import es.killobyte.nucleo.common.service.impl.ConfigService
import org.reflections.Reflections
import java.lang.reflect.Modifier

@Singleton
class TaskLoader @Inject constructor(
    private val plugin: NucleoPlugin,
    private val injector: Injector,
    private val reflections: Reflections,
    private val moduleLoader: ModuleLoader,
    private val configService: ConfigService
) {
    private val tasks = mutableListOf<ITask>()
    private val running = mutableSetOf<ITask>()

    fun loadAll() {
        val taskClasses = reflections.getSubTypesOf(ITask::class.java)

        var loadedCount = 0
        for (clazz in taskClasses) {
            if (clazz.isInterface || Modifier.isAbstract(clazz.modifiers)) continue

            runCatching {
                val taskInstance = injector.getInstance(clazz)
                tasks.add(taskInstance)
                loadedCount++
            }.onFailure { e ->
                plugin.logger.severe("Error instantiating task from class: ${clazz.name}")
                e.printStackTrace()
            }
        }
        plugin.logger.info("Loaded $loadedCount tasks!")
    }

    fun startAll() {
        tasks.filter { canRun(it) }.forEach { start(it) }
    }

    fun stopAll() {
        running.reversed().forEach { stop(it) }
        tasks.clear()
    }

    fun onModuleToggled(moduleId: String) {
        tasks.filter { requiredModuleId(it) == moduleId }.forEach { task ->
            if (canRun(task)) start(task) else stop(task)
        }
    }

    private fun start(task: ITask) {
        if (!running.add(task)) return
        runCatching { task.start() }.onFailure { e ->
            running.remove(task)
            plugin.logger.severe("Error starting task ${task.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun stop(task: ITask) {
        if (!running.remove(task)) return
        runCatching { task.stop() }.onFailure { e ->
            plugin.logger.severe("Error stopping task ${task.javaClass.simpleName}: ${e.message}")
        }
    }

    private fun requiredModuleId(task: ITask): String? =
        task.javaClass.getAnnotation(RequiresModule::class.java)?.module?.removePrefix("modules.")

    private fun canRun(task: ITask): Boolean {
        val annotation = task.javaClass.getAnnotation(RequiresModule::class.java) ?: return true
        if (!moduleLoader.isModuleEnabled(annotation.module.removePrefix("modules."))) return false
        return annotation.feature.isEmpty() || configService.getBoolean(annotation.feature, true)
    }
}
