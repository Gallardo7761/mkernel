package net.miarma.mkernel.common.task

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Singleton
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.common.ITask
import org.reflections.Reflections
import java.lang.reflect.Modifier

@Singleton
class TaskLoader @Inject constructor(
    private val plugin: MKernel,
    private val injector: Injector
) {
    private val tasks = mutableListOf<ITask>()

    fun loadAll() {
        val reflections = Reflections("net.miarma.mkernel")
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
        tasks.forEach { it.start() }
    }

    fun stopAll() {
        tasks.reversed().forEach { runCatching { it.stop() } }
        tasks.clear()
    }
}