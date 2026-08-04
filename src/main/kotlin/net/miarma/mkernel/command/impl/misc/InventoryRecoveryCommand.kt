package net.miarma.mkernel.command.impl.misc

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.annotation.RequiresModule
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.DatabaseService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.CommandUtil.checkModule
import org.bukkit.Material

@Singleton
@RequiresModule(ConfigKeys.Modules.Player.MAIN, ConfigKeys.Modules.Player.RECOVER_INVENTORY)
class InventoryRecoveryCommand @Inject constructor(
    private val plugin: MKernel,
    private val configService: ConfigService,
    private val databaseService: DatabaseService,
    private val messageService: MessageService,
    private val moduleLoader: ModuleLoader
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.RecInv.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Player.MAIN, ConfigKeys.Modules.Player.RECOVER_INVENTORY)
            withPermission(configService.getString(ConfigKeys.Commands.RecInv.PERM))
            withShortDescription(configService.getString(ConfigKeys.Commands.RecInv.DESC))
            playerExecutor { sender, _ ->
                val xpLevels = sender.level
                val requiredLevels = configService.getInt(ConfigKeys.Settings.Death.REC_INV_LEVEL)

                if (xpLevels < requiredLevels) {
                    messageService.builder(configService.getString(ConfigKeys.Messages.Death.Errors.NOT_ENOUGH_LEVELS)).withPrefix().tag("required", requiredLevels.toString()).send(sender)
                    return@playerExecutor
                }

                val inventoryId = sender.uniqueId.toString()

                plugin.launchSync {
                    val items = databaseService.loadInventory(inventoryId)

                    if (items.isEmpty()) {
                        messageService.builder(configService.getString(ConfigKeys.Messages.Death.Errors.NO_ITEMS)).withPrefix().send(sender)
                        return@launchSync
                    }

                    items.filterNotNull()
                        .filter { it.type != Material.AIR }
                        .forEach { sender.inventory.addItem(it) }

                    val totalItems = items.filterNotNull().filter { it.type != Material.AIR }.sumOf { it.amount }

                    messageService.builder(configService.getString(ConfigKeys.Commands.RecInv.MSG_RECOVERED)).withPrefix().tag("items", totalItems.toString()).send(sender)
                    sender.level = xpLevels - requiredLevels
                    databaseService.deleteInventory(inventoryId)
                }
            }
        }
    }
}