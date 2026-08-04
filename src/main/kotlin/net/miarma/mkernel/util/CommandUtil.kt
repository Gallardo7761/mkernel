package net.miarma.mkernel.util

import dev.jorel.commandapi.CommandAPICommand
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import org.bukkit.command.CommandSender

object CommandUtil {
    fun CommandAPICommand.checkModule(
        moduleLoader: ModuleLoader,
        configService: ConfigService,
        modulePath: String,
        featurePath: String = ""
    ) {
        withRequirement { sender: CommandSender ->
            val moduleId = modulePath.removePrefix("modules.")

            if (!moduleLoader.isModuleEnabled(moduleId)) {
                return@withRequirement false
            }

            if (featurePath.isNotEmpty() && !configService.getBoolean(featurePath, true)) {
                return@withRequirement false
            }

            true
        }
    }
}