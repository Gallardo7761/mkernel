package net.miarma.mkernel.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.annotation.LoaderPriority
import net.miarma.mkernel.api.common.IService
import net.miarma.mkernel.common.config.ConfigKeys
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.SimpleCommandMap

@Singleton
@LoaderPriority(LoaderPriority.LOWEST)
class BlacklistService @Inject constructor(private val configService: ConfigService) : IService {

    override fun onEnable() {
        unregisterCommands()
        unregisterRecipes()
    }

    fun unregisterCommands() {
        val blacklistedCommands = configService.getStringList(ConfigKeys.Settings.Blacklist.COMMANDS)

        try {
            val server = Bukkit.getServer()
            val commandMapField = server.javaClass.getDeclaredField("commandMap").apply { isAccessible = true }
            val commandMap = commandMapField.get(server) as SimpleCommandMap

            val knownCommandsField = SimpleCommandMap::class.java.getDeclaredField("knownCommands").apply { isAccessible = true }
            @Suppress("UNCHECKED_CAST")
            val knownCommands = knownCommandsField.get(commandMap) as MutableMap<String, Command>

            for (cmd in blacklistedCommands) {
                val cmdName = cmd.lowercase().removePrefix("/")
                knownCommands[cmdName]?.unregister(commandMap)
                knownCommands.remove(cmdName)
                knownCommands.keys.removeIf { it.endsWith(":$cmdName") }
            }
            MKernel.LOGGER.info("Unregistered commands in blacklist")
        } catch (e: Exception) {
            MKernel.LOGGER.severe("Error unregistering commands in blacklist: ${e.message}")
        }
    }

    fun unregisterRecipes() {
        val blacklistedRecipes = configService.getStringList(ConfigKeys.Settings.Blacklist.RECIPES)

        for (recipeStr in blacklistedRecipes) {
            val key = NamespacedKey.fromString(recipeStr) ?: NamespacedKey.minecraft(recipeStr.lowercase())
            Bukkit.removeRecipe(key)
        }
        MKernel.LOGGER.info("Unregistered recipes in blacklist")
    }
}
