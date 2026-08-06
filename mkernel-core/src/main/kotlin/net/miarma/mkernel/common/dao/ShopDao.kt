package net.miarma.mkernel.common.dao

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.api.model.Shop
import net.miarma.mkernel.common.service.impl.DatabaseService
import net.miarma.mkernel.common.service.impl.InventoryService
import org.bukkit.Bukkit
import org.bukkit.Location
import java.sql.ResultSet
import java.util.*

@Singleton
class ShopDao @Inject constructor(
    private val dbService: DatabaseService,
    private val userDao: UserDao,
    private val worldDao: WorldDao
) {

    suspend fun getAllShops(): List<Shop> = dbService.withConnection { conn ->
        val shops = mutableListOf<Shop>()
        conn.prepareStatement("SELECT s.shop_id, s.owner_uuid, w.name AS world_name, s.x, s.y, s.z, s.item_data," +
                " s.price, s.stock FROM Shop s LEFT JOIN World w ON s.world_id = w.world_id").use { stmt ->
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    val shop = mapResultSetToShop(rs)
                    if (shop != null) {
                        shops.add(shop)
                    }
                }
            }
        }
        shops
    }

    private fun mapResultSetToShop(rs: ResultSet): Shop? {
        return try {
            val id = rs.getString("shop_id")
            val ownerUuid = UUID.fromString(rs.getString("owner_uuid"))

            val worldName = rs.getString("world_name") ?: return null
            val world = Bukkit.getWorld(worldName) ?: run {
                return null
            }

            val x = rs.getInt("x").toDouble()
            val y = rs.getInt("y").toDouble()
            val z = rs.getInt("z").toDouble()
            val location = Location(world, x, y, z)

            val price = rs.getDouble("price")
            val stock = rs.getInt("stock")

            val bytes = rs.getBytes("item_data")
            val itemArray = InventoryService.fromBase64(bytes)
            val item = itemArray.firstOrNull() ?: return null

            Shop(
                id = id,
                ownerUuid = ownerUuid,
                location = location,
                item = item,
                price = price,
                stock = stock
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun insertShop(shop: Shop) = dbService.withConnection { conn ->
        userDao.ensureUserExists(conn, Bukkit.getPlayer(shop.ownerUuid)!!)
        worldDao.ensureWorldExists(conn, shop.location.world)

        conn.prepareStatement(
            "INSERT OR REPLACE INTO Shop (shop_id, owner_uuid, world_id, x, y, z, item_data, price, stock) " +
                    "VALUES (?, ?, (SELECT world_id FROM World WHERE name = ?), ?, ?, ?, ?, ?, ?)"
        ).use { ps ->
            ps.setString(1, shop.id)
            ps.setString(2, shop.ownerUuid.toString())
            ps.setString(3, shop.location.world.name)
            ps.setInt(4, shop.location.blockX)
            ps.setInt(5, shop.location.blockY)
            ps.setInt(6, shop.location.blockZ)
            ps.setBytes(7, InventoryService.toBase64(arrayOf(shop.item)))
            ps.setDouble(8, shop.price)
            ps.setInt(9, shop.stock)
            ps.executeUpdate()
        }
    }

    suspend fun updateShopStock(shopId: String, newStock: Int) = dbService.withConnection { conn ->
        conn.prepareStatement("UPDATE Shop SET stock = ? WHERE shop_id = ?").use { ps ->
            ps.setInt(1, newStock)
            ps.setString(2, shopId)
            ps.executeUpdate()
        }
    }

    suspend fun deleteShop(shopId: String) = dbService.withConnection { conn ->
        conn.prepareStatement("DELETE FROM Shop WHERE shop_id = ?").use { ps ->
            ps.setString(1, shopId)
            ps.executeUpdate()
        }
    }
}