package es.killobyte.nucleo.command

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Singleton
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.annotation.RequiresModule
import es.killobyte.nucleo.api.common.ICommand
import es.killobyte.nucleo.common.service.impl.ConfigService
import org.reflections.Reflections
import java.lang.reflect.Modifier

@Singleton
class CommandHandler @Inject constructor(
    private val plugin: NucleoPlugin,
    private val injector: Injector,
    private val reflections: Reflections
) {
    fun registerCommands() {
        val commandClasses = reflections.getSubTypesOf(ICommand::class.java)
        var commands = 0

        for (commandClass in commandClasses) {
            if (commandClass.isInterface || Modifier.isAbstract(commandClass.modifiers)) continue
            if (!checkModuleAccess(commandClass)) continue

            try {
                val commandInstance = injector.getInstance(commandClass)
                commandInstance.register()
                commands++
            } catch (e: Exception) {
                plugin.logger.severe("Failed to register command from class: ${commandClass.name}")
                e.printStackTrace()
            }
        }
        plugin.logger.info("Loaded $commands commands!")
    }

    private fun checkModuleAccess(commandClass: Class<*>): Boolean {
        val annotation = commandClass.getAnnotation(RequiresModule::class.java) ?: return true
        val configService = injector.getInstance(ConfigService::class.java)

        val isModuleEnabled = configService.getBoolean("${annotation.module}.enabled", true)
                || configService.getBoolean(annotation.module, true)

        if (!isModuleEnabled) return false

        if (annotation.feature.isNotEmpty()) {
            return configService.getBoolean(annotation.feature, true)
        }

        return true
    }
}
