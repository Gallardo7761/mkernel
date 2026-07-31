package net.miarma.mkernel.command

import MKernel
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Singleton
import org.reflections.Reflections
import org.reflections.scanners.Scanners

@Singleton
class CommandHandler @Inject constructor(private val injector: Injector) {

    fun registerCommands() {
        val commandsPackage = "net.miarma.mkernel.command"
        val reflections = Reflections(commandsPackage, Scanners.SubTypes.filterResultsBy { true })
        val commandClasses = reflections.getSubTypesOf(Any::class.java)
        var commands = 0

        for (commandClass in commandClasses) {
            try {
                val registerMethod = commandClass.getMethod("register")
                if (!java.lang.reflect.Modifier.isStatic(registerMethod.modifiers) && registerMethod.returnType == Void.TYPE) {
                    val commandInstance = injector.getInstance(commandClass)
                    registerMethod.invoke(commandInstance)
                }
                commands++
            } catch (ignored: NoSuchMethodException) {
                // not a command
            } catch (e: Exception) {
                MKernel.LOGGER.severe("Failed to register command from class: ${commandClass.name}")
                e.printStackTrace()
            }
        }
        MKernel.LOGGER.info("$commands commands registered!")
    }
}
