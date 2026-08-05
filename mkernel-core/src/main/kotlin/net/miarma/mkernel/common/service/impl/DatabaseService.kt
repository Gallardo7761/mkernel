package net.miarma.mkernel.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.annotation.LoaderPriority
import net.miarma.mkernel.api.common.IService
import net.miarma.mkernel.api.model.Shop
import net.miarma.mkernel.api.model.Warp
import net.miarma.mkernel.common.teleport.TpaRequest
import net.miarma.mkernel.common.teleport.TpaType
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

@Singleton
@LoaderPriority(LoaderPriority.HIGHEST)
class DatabaseService @Inject constructor(private val plugin: MKernel) : IService {

    private val dbFile = File(plugin.dataFolder, "database.db")
    private val rootSqlFile = File("init.sql")
    private var connection: Connection? = null
    private val dbDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val homeCache = ConcurrentHashMap<UUID, Location>()
    private val warpCache = ConcurrentHashMap<UUID, MutableSet<Warp>>()

    override fun onEnable() {
        runBlocking {
            try {
                connect()
                initTables()
                Bukkit.getWorlds().forEach { createWorld(it) }
                MKernel.LOGGER.info("Database connected and tables initialized successfully!")
            } catch (e: SQLException) {
                MKernel.LOGGER.severe("Could not establish connection to SQLite!")
                e.printStackTrace()
            }
        }
    }

    override fun onDisable() {
        plugin.launchAsync {
            try {
                disconnect()
                MKernel.LOGGER.info("Database connection closed successfully.")
            } catch (e: SQLException) {
                MKernel.LOGGER.severe("Could not close database connection: ${e.message}")
            }
        }
    }

    private suspend fun connect() = withContext(dbDispatcher) {
        if (connection?.isClosed == false) return@withContext
        if (!dbFile.parentFile.exists()) {
            dbFile.parentFile.mkdirs()
        }
        val url = "jdbc:sqlite:${dbFile.absolutePath}"
        connection = DriverManager.getConnection(url)
        connection?.createStatement()?.use {
            it.execute("PRAGMA foreign_keys = ON;")
            it.execute("PRAGMA journal_mode = WAL;")
        }
    }

    private suspend fun disconnect() = withContext(dbDispatcher) {
        connection?.takeIf { !it.isClosed }?.close()
    }

    private suspend fun getConnection(): Connection {
        if (connection?.isClosed != false) {
            connect()
        }
        return connection!!
    }

    private suspend fun initTables() = withContext(dbDispatcher) {
        val stream = plugin.getResource("init.sql")
            ?: error("init.sql not found. Things will not work!")

        val sqlScript = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val statements = sqlScript.split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val conn = getConnection()
        conn.createStatement().use { stmt ->
            for (sql in statements) {
                stmt.execute(sql)
            }
        }
    }

    private suspend fun ensureUserAndWorldExist(conn: Connection, player: Player?, world: World?) = withContext(dbDispatcher) {
        player?.let {
            conn.prepareStatement("INSERT OR IGNORE INTO User (uuid, name) VALUES (?, ?)").use { ps ->
                ps.setString(1, it.uniqueId.toString())
                ps.setString(2, it.name)
                ps.executeUpdate()
            }
        }
        world?.let {
            conn.prepareStatement("INSERT OR IGNORE INTO World (name, is_blocked) VALUES (?, 0)").use { ps ->
                ps.setString(1, it.name)
                ps.executeUpdate()
            }
        }
    }

    private fun createWorld(world: World) {
        plugin.launchAsync {
            ensureUserAndWorldExist(getConnection(), null, world)
        }
    }

    fun loadPlayerData(player: Player) {
        plugin.launchAsync {
            val uuid = player.uniqueId

            val home = fetchHomeFromDb(player)
            if (home != null)
                homeCache[uuid] = home

            val warps = fetchWarpsFromDb(player)
            warpCache[uuid] = ConcurrentHashMap.newKeySet<Warp>().apply { addAll(warps) }
        }
    }

    fun unloadPlayerData(player: Player) {
        val uuid = player.uniqueId
        homeCache.remove(uuid)
        warpCache.remove(uuid)
    }

    private suspend fun fetchHomeFromDb(player: Player): Location? = withContext(dbDispatcher) {
        getConnection().prepareStatement(
            "SELECT w.name, h.x, h.y, h.z, h.yaw, h.pitch " +
                    "FROM Home h JOIN World w ON h.world_id = w.world_id " +
                    "WHERE h.owner_uuid = ?"
        ).use { ps ->
            ps.setString(1, player.uniqueId.toString())
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    val worldName = rs.getString("name")
                    val x = rs.getDouble("x")
                    val y = rs.getDouble("y")
                    val z = rs.getDouble("z")
                    val yaw = rs.getFloat("yaw")
                    val pitch = rs.getFloat("pitch")
                    Bukkit.getWorld(worldName)?.let { Location(it, x, y, z, yaw, pitch) }
                } else null
            }
        }
    }

    private suspend fun fetchWarpsFromDb(player: Player): Set<Warp> = withContext(dbDispatcher) {
        getConnection().prepareStatement(
            "SELECT wa.warp_name, wa.x, wa.y, wa.z, w.name " +
                    "FROM Warp wa JOIN World w ON wa.world_id = w.world_id " +
                    "WHERE wa.owner_uuid = ?"
        ).use { ps ->
            ps.setString(1, player.uniqueId.toString())
            ps.executeQuery().use { rs ->
                generateSequence {
                    if (rs.next()) {
                        Warp(
                            rs.getString("warp_name"),
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("z"),
                            rs.getString("name")
                        )
                    } else null
                }.toSet()
            }
        }
    }

    fun getHome(player: Player): Location? {
        return homeCache[player.uniqueId]
    }

    fun setHome(player: Player, location: Location) {
        homeCache[player.uniqueId] = location

        plugin.launchAsync {
            withContext(dbDispatcher) {
                val conn = getConnection()
                ensureUserAndWorldExist(conn, player, location.world)
                conn.prepareStatement(
                    "INSERT OR REPLACE INTO Home (owner_uuid, world_id, x, y, z, yaw, pitch) " +
                            "VALUES (?, (SELECT world_id FROM World WHERE name = ?), ?, ?, ?, ?, ?)"
                ).use { ps ->
                    ps.setString(1, player.uniqueId.toString())
                    ps.setString(2, location.world.name)
                    ps.setDouble(3, location.x)
                    ps.setDouble(4, location.y)
                    ps.setDouble(5, location.z)
                    ps.setFloat(6, location.yaw)
                    ps.setFloat(7, location.pitch)
                    ps.executeUpdate()
                }
            }
        }
    }

    fun getWarpCount(player: Player): Int {
        return warpCache[player.uniqueId]?.size ?: 0
    }

    fun warpExists(player: Player, warpName: String): Boolean {
        return warpCache[player.uniqueId]?.any { it.alias.equals(warpName, ignoreCase = true) } ?: false
    }

    fun getWarpObjects(player: Player): Set<Warp> {
        return warpCache[player.uniqueId] ?: emptySet()
    }

    fun createWarp(player: Player, warpName: String, location: Location) {
        val newWarp = Warp(warpName, location.x, location.y, location.z, location.world.name)

        warpCache.computeIfAbsent(player.uniqueId) { ConcurrentHashMap.newKeySet() }.add(newWarp)

        plugin.launchAsync {
            withContext(dbDispatcher) {
                val conn = getConnection()
                ensureUserAndWorldExist(conn, player, location.world)
                conn.prepareStatement(
                    "INSERT OR REPLACE INTO Warp (owner_uuid, world_id, warp_name, x, y, z, yaw, pitch) " +
                            "VALUES (?, (SELECT world_id FROM World WHERE name = ?), ?, ?, ?, ?, ?, ?)"
                ).use { ps ->
                    ps.setString(1, player.uniqueId.toString())
                    ps.setString(2, location.world.name)
                    ps.setString(3, warpName)
                    ps.setDouble(4, location.x)
                    ps.setDouble(5, location.y)
                    ps.setDouble(6, location.z)
                    ps.setFloat(7, location.yaw)
                    ps.setFloat(8, location.pitch)
                    ps.executeUpdate()
                }
            }
        }
    }

    fun deleteWarp(player: Player, warpName: String) {
        warpCache[player.uniqueId]?.removeIf { it.alias.equals(warpName, ignoreCase = true) }

        plugin.launchAsync {
            withContext(dbDispatcher) {
                getConnection().prepareStatement("DELETE FROM Warp WHERE owner_uuid = ? AND warp_name = ?").use { ps ->
                    ps.setString(1, player.uniqueId.toString())
                    ps.setString(2, warpName)
                    ps.executeUpdate()
                }
            }
        }
    }

    fun saveInventory(inventoryId: String, items: Array<ItemStack?>) {
        plugin.launchAsync {
            saveInventorySync(inventoryId, items)
        }
    }

    private suspend fun saveInventorySync(inventoryId: String, items: Array<ItemStack?>) = withContext(dbDispatcher) {
        getConnection().prepareStatement("INSERT OR REPLACE INTO Inventory (inventory_id, data) VALUES (?, ?)").use { ps ->
            ps.setString(1, inventoryId)
            ps.setBytes(2, InventoryService.toBase64(items))
            ps.executeUpdate()
        }
    }

    suspend fun loadInventory(inventoryId: String): Array<ItemStack?> = withContext(dbDispatcher) {
        getConnection().prepareStatement("SELECT data FROM Inventory WHERE inventory_id = ?").use { ps ->
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

    fun deleteInventory(inventoryId: String) {
        plugin.launchAsync {
            withContext(dbDispatcher) {
                getConnection().prepareStatement("DELETE FROM Inventory WHERE inventory_id = ?").use { ps ->
                    ps.setString(1, inventoryId)
                    ps.executeUpdate()
                }
            }
        }
    }

    suspend fun getBlockedWorlds(): List<String> = withContext(dbDispatcher) {
        getConnection().prepareStatement("SELECT name FROM World WHERE is_blocked = 1").use { ps ->
            ps.executeQuery().use { rs ->
                generateSequence { if (rs.next()) rs.getString("name") else null }.toList()
            }
        }
    }

    suspend fun isWorldBlocked(worldName: String): Boolean = withContext(dbDispatcher) {
        getConnection().prepareStatement("SELECT is_blocked FROM World WHERE name = ?").use { ps ->
            ps.setString(1, worldName)
            ps.executeQuery().use { rs -> if (rs.next()) rs.getInt("is_blocked") == 1 else false }
        }
    }

    fun setWorldBlocked(worldName: String, blocked: Boolean) {
        plugin.launchAsync {
            withContext(dbDispatcher) {
                val conn = getConnection()
                Bukkit.getWorld(worldName)?.let { ensureUserAndWorldExist(conn, null, it) }
                conn.prepareStatement("UPDATE World SET is_blocked = ? WHERE name = ?").use { ps ->
                    ps.setInt(1, if (blocked) 1 else 0)
                    ps.setString(2, worldName)
                    ps.executeUpdate()
                }
            }
        }
    }

    fun insertTeleportRequest(senderUUID: String, receiverUUID: String, isTpa: Boolean) {
        plugin.launchAsync {
            withContext(dbDispatcher) {
                val conn = getConnection()
                val sender = Bukkit.getPlayer(UUID.fromString(senderUUID))
                val receiver = Bukkit.getPlayer(UUID.fromString(receiverUUID))
                ensureUserAndWorldExist(conn, sender, null)
                ensureUserAndWorldExist(conn, receiver, null)
                conn.prepareStatement("INSERT OR REPLACE INTO Teleport (sender_uuid, receiver_uuid, is_tpa) VALUES (?, ?, ?)").use { ps ->
                    ps.setString(1, senderUUID)
                    ps.setString(2, receiverUUID)
                    ps.setInt(3, if (isTpa) 1 else 0)
                    ps.executeUpdate()
                }
            }
        }
    }

    fun deleteTeleportRequest(senderUUID: String, receiverUUID: String) {
        plugin.launchAsync {
            withContext(dbDispatcher) {
                getConnection().prepareStatement("DELETE FROM Teleport WHERE sender_uuid = ? AND receiver_uuid = ?").use { ps ->
                    ps.setString(1, senderUUID)
                    ps.setString(2, receiverUUID)
                    ps.executeUpdate()
                }
            }
        }
    }

    suspend fun getTpaRequest(from: Player, to: Player): TpaRequest? = withContext(dbDispatcher) {
        getConnection().prepareStatement("SELECT is_tpa FROM Teleport WHERE sender_uuid = ? AND receiver_uuid = ?").use { ps ->
            ps.setString(1, from.uniqueId.toString())
            ps.setString(2, to.uniqueId.toString())
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    val type = if (rs.getInt("is_tpa") == 1) TpaType.TPA else TpaType.TPA_HERE
                    TpaRequest(from, to, type)
                } else {
                    null
                }
            }
        }
    }

    suspend fun getIncomingTpaRequest(receiver: Player): TpaRequest? {
        val data = withContext(dbDispatcher) {
            getConnection().prepareStatement("SELECT sender_uuid, is_tpa FROM Teleport WHERE receiver_uuid = ? LIMIT 1").use { ps ->
                ps.setString(1, receiver.uniqueId.toString())
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        val senderUUID = UUID.fromString(rs.getString("sender_uuid"))
                        val type = if (rs.getInt("is_tpa") == 1) TpaType.TPA else TpaType.TPA_HERE
                        Pair(senderUUID, type)
                    } else null
                }
            }
        }

        if (data == null) return null

        return withContext(plugin.syncDispatcher) {
            val (senderUUID, type) = data
            val sender = Bukkit.getPlayer(senderUUID)

            if (sender?.isOnline == true) {
                TpaRequest(sender, receiver, type)
            } else {
                deleteTeleportRequest(senderUUID.toString(), receiver.uniqueId.toString())
                null
            }
        }
    }

    fun saveInventoryBytesSync(inventoryId: String, data: ByteArray) {
        plugin.launchAsync {
            withContext(dbDispatcher) {
                getConnection().prepareStatement("INSERT OR REPLACE INTO Inventory (inventory_id, data) VALUES (?, ?)").use { ps ->
                    ps.setString(1, inventoryId)
                    ps.setBytes(2, data)
                    ps.executeUpdate()
                }
            }
        }
    }

    suspend fun loadInventoryBytes(inventoryId: String): ByteArray? = withContext(dbDispatcher) {
        getConnection().prepareStatement("SELECT data FROM Inventory WHERE inventory_id = ?").use { ps ->
            ps.setString(1, inventoryId)
            ps.executeQuery().use { rs ->
                if (rs.next()) rs.getBytes("data") else null
            }
        }
    }

    suspend fun getAllShops(): List<Shop> = withContext(dbDispatcher) {
        val shops = mutableListOf<Shop>()

        getConnection().use { conn ->
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

    suspend fun insertShop(shop: Shop) = withContext(dbDispatcher) {
        val conn = getConnection()
        ensureUserAndWorldExist(conn, Bukkit.getPlayer(shop.ownerUuid), shop.location.world)

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

    suspend fun updateShopStock(shopId: String, newStock: Int) = withContext(dbDispatcher) {
        getConnection().prepareStatement("UPDATE Shop SET stock = ? WHERE shop_id = ?").use { ps ->
            ps.setInt(1, newStock)
            ps.setString(2, shopId)
            ps.executeUpdate()
        }
    }

    suspend fun deleteShop(shopId: String) = withContext(dbDispatcher) {
        getConnection().prepareStatement("DELETE FROM Shop WHERE shop_id = ?").use { ps ->
            ps.setString(1, shopId)
            ps.executeUpdate()
        }
    }
}