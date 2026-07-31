package net.miarma.mkernel.command.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.DatabaseService
import net.miarma.mkernel.common.service.impl.MessageService
import org.bukkit.Material

@Singleton
class InventoryRecoveryCommand @Inject constructor(
    private val configService: ConfigService,
    private val databaseService: DatabaseService,
    private val messageService: MessageService
) {
    fun register() {
        commandAPICommand(configService.getString("commands.recinv.name")) {
            withPermission(configService.getString("commands.recinv.permission"))
            withShortDescription(configService.getString("commands.recinv.description"))
            playerExecutor { sender, _ ->
                if (!configService.isModuleEnabled("recoverInventory")) {
                    messageService.builder(configService.getString("language.errors.temporarilyDisabled")).withPrefix().send(sender)
                    return@playerExecutor
                }

                val xpLevels = sender.level
                val requiredLevels = configService.getInt("config.values.recInvRequiredLevel")

                if (xpLevels < requiredLevels) {
                    messageService.builder(configService.getString("commands.recinv.messages.notEnoughLevels")).withPrefix().tag("required", requiredLevels.toString()).send(sender)
                    return@playerExecutor
                }

                val inventoryId = sender.uniqueId.toString()
                databaseService.loadInventory(inventoryId) { items ->
                    if (items.isNullOrEmpty()) {
                        messageService.builder(configService.getString("commands.recinv.messages.noItemsToRecover")).withPrefix().send(sender)
                        return@loadInventory
                    }

                    items.filterNotNull()
                         .filter { it.type != Material.AIR }
                         .forEach { sender.inventory.addItem(it) }

                    val totalItems = items.filterNotNull().filter { it.type != Material.AIR }.sumOf { it.amount }

                    messageService.builder(configService.getString("commands.recinv.messages.inventoryRecovered")).withPrefix().tag("items", totalItems.toString()).send(sender)
                    sender.level = xpLevels - requiredLevels
                    databaseService.deleteInventory(inventoryId)
                }
            }
        }
    }
}