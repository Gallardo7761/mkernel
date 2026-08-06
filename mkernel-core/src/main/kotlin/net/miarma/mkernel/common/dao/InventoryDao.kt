package net.miarma.mkernel.common.dao

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.service.impl.DatabaseService
import net.miarma.mkernel.common.service.impl.InventoryService
import org.bukkit.inventory.ItemStack

@Singleton
class InventoryDao @Inject constructor(private val dbService: DatabaseService) {

    suspend fun saveInventory(inventoryId: String, items: Array<ItemStack?>) {
        dbService.withConnection { conn ->
            conn.prepareStatement("INSERT OR REPLACE INTO Inventory (inventory_id, data) VALUES (?, ?)").use { ps ->
                ps.setString(1, inventoryId)
                ps.setBytes(2, InventoryService.toBase64(items))
                ps.executeUpdate()
            }
        }
    }

    suspend fun loadInventory(inventoryId: String): Array<ItemStack?> = dbService.withConnection { conn ->
        conn.prepareStatement("SELECT data FROM Inventory WHERE inventory_id = ?").use { ps ->
            ps.setString(1, inventoryId)
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    InventoryService.fromBase64(rs.getBytes("data"))
                } else {
                    emptyArray()
                }
            }
        }
    }

    suspend fun deleteInventory(inventoryId: String) {
        dbService.withConnection { conn ->
            conn.prepareStatement("DELETE FROM Inventory WHERE inventory_id = ?").use { ps ->
                ps.setString(1, inventoryId)
                ps.executeUpdate()
            }
        }
    }

    suspend fun saveInventoryBytes(inventoryId: String, data: ByteArray) {
        dbService.withConnection { conn ->
            conn.prepareStatement("INSERT OR REPLACE INTO Inventory (inventory_id, data) VALUES (?, ?)").use { ps ->
                ps.setString(1, inventoryId)
                ps.setBytes(2, data)
                ps.executeUpdate()
            }
        }
    }

    suspend fun loadInventoryBytes(inventoryId: String): ByteArray? = dbService.withConnection { conn ->
        conn.prepareStatement("SELECT data FROM Inventory WHERE inventory_id = ?").use { ps ->
            ps.setString(1, inventoryId)
            ps.executeQuery().use { rs ->
                if (rs.next()) rs.getBytes("data") else null
            }
        }
    }
}