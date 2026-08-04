package net.miarma.mkernel.util

import dev.jorel.commandapi.CommandAPICommand
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import org.bukkit.command.CommandSender

object CommandUtil {
    fun CommandAPICommand.checkModule(
        moduleLoader: ModuleLoader,
        configService: ConfigService,
        messageService: MessageService,
        modulePath: String,
        featurePath: String = ""
    ) {
        withRequirement { sender: CommandSender ->
            val moduleId = modulePath.removePrefix("modules.")

            if (!moduleLoader.isModuleEnabled(moduleId)) {
                messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.TEMPORARILY_DISABLED))
                    .withPrefix()
                    .send(sender)
                return@withRequirement false
            }

            if (featurePath.isNotEmpty() && !configService.getBoolean(featurePath, true)) {
                messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.TEMPORARILY_DISABLED))
                    .withPrefix()
                    .send(sender)
                return@withRequirement false
            }

            true
        }
    }
}