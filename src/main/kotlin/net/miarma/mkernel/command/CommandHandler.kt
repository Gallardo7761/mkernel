package net.miarma.mkernel.command

import MKernel
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Singleton
import net.miarma.mkernel.common.annotation.RequiresModule
import net.miarma.mkernel.common.service.impl.ConfigService
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import java.lang.reflect.Modifier

@Singleton
class CommandHandler @Inject constructor(private val injector: Injector) {
    fun registerCommands() {
        val commandsPackage = "net.miarma.mkernel.command"
        val reflections = Reflections(commandsPackage, Scanners.SubTypes.filterResultsBy { true })
        val commandClasses = reflections.getSubTypesOf(MCommand::class.java)
        var commands = 0

        for (commandClass in commandClasses) {
            if (commandClass.isInterface || Modifier.isAbstract(commandClass.modifiers)) continue
            if (!checkModuleAccess(commandClass)) continue

            try {
                val commandInstance = injector.getInstance(commandClass)
                commandInstance.register()
                commands++
            } catch (e: Exception) {
                MKernel.LOGGER.severe("Failed to register command from class: ${commandClass.name}")
                e.printStackTrace()
            }
        }
        MKernel.LOGGER.info("Loaded $commands commands!")
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
