package net.miarma.mkernel.common.service.impl

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.miarma.mkernel.common.model.Warp
import net.miarma.mkernel.common.service.IService
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
import java.sql.SQLException
import java.util.*
import java.util.concurrent.Executors
import java.util.function.Consumer

@Singleton
class DatabaseService @Inject constructor(private val plugin: MKernel) : IService {

    private val dbFile = File(plugin.dataFolder, "database.db")
    private var connection: Connection? = null
    private val dbDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

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
        val conn = getConnection()
        conn.createStatement().use { stmt ->
            stmt.execute("CREATE TABLE IF NOT EXISTS User (uuid TEXT PRIMARY KEY, name TEXT NOT NULL);")
            stmt.execute(
                """
            CREATE TABLE IF NOT EXISTS World (
                world_id INTEGER PRIMARY KEY,
                name TEXT NOT NULL UNIQUE,
                is_blocked INTEGER NOT NULL
            );
        """
            )
            stmt.execute(
                """
            CREATE TABLE IF NOT EXISTS Home (
                owner_uuid TEXT PRIMARY KEY,
                world_id INTEGER NOT NULL,
                x REAL NOT NULL,
                y REAL NOT NULL,
                z REAL NOT NULL,
                yaw REAL NOT NULL,
                pitch REAL NOT NULL,
                FOREIGN KEY (owner_uuid) REFERENCES User(uuid),
                FOREIGN KEY (world_id) REFERENCES World(world_id)
            );
        """
            )
            stmt.execute(
                """
            CREATE TABLE IF NOT EXISTS Warp (
                warp_id INTEGER PRIMARY KEY,
                owner_uuid TEXT NOT NULL,
                world_id INTEGER NOT NULL,
                warp_name TEXT NOT NULL,
                x REAL NOT NULL,
                y REAL NOT NULL,
                z REAL NOT NULL,
                yaw REAL NOT NULL,
                pitch REAL NOT NULL,
                FOREIGN KEY (owner_uuid) REFERENCES User(uuid),
                FOREIGN KEY (world_id) REFERENCES World(world_id),
                UNIQUE (owner_uuid, warp_name)
            );
        """
            )
            stmt.execute(
                """
            CREATE TABLE IF NOT EXISTS Inventory (
                inventory_id TEXT PRIMARY KEY,
                data BLOB NOT NULL
            );
        """
            )
            stmt.execute(
                """
            CREATE TABLE IF NOT EXISTS Teleport (
                request_id INTEGER PRIMARY KEY,
                sender_uuid TEXT NOT NULL,
                receiver_uuid TEXT NOT NULL,
                is_tpa INTEGER NOT NULL DEFAULT 0,
                timeout INTEGER NOT NULL DEFAULT 60,
                FOREIGN KEY (sender_uuid) REFERENCES User(uuid),
                FOREIGN KEY (receiver_uuid) REFERENCES User(uuid),
                UNIQUE (sender_uuid, receiver_uuid)
            );
        """
            )
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

    suspend fun getHome(player: Player): Location? = withContext(dbDispatcher) {
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
                } else {
                    null
                }
            }
        }
    }

    fun setHome(player: Player, location: Location) {
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

    fun createWarp(player: Player, warpName: String, location: Location) {
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

    fun loadInventory(inventoryId: String, callback: Consumer<Array<ItemStack?>>) {
        plugin.launchAsync {
            val items = withContext(dbDispatcher) {
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
            callback.accept(items)
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

    fun getBlockedWorlds(callback: Consumer<List<String>>) {
        plugin.launchAsync {
            val worlds = withContext(dbDispatcher) {
                getConnection().prepareStatement("SELECT name FROM World WHERE is_blocked = 1").use { ps ->
                    ps.executeQuery().use { rs ->
                        generateSequence { if (rs.next()) rs.getString("name") else null }.toList()
                    }
                }
            }
            callback.accept(worlds)
        }
    }

    fun getWarpCount(player: Player, callback: Consumer<Int>) {
        plugin.launchAsync {
            val count = withContext(dbDispatcher) {
                getConnection().prepareStatement("SELECT COUNT(*) FROM Warp WHERE owner_uuid = ?").use { ps ->
                    ps.setString(1, player.uniqueId.toString())
                    ps.executeQuery().use { rs -> if (rs.next()) rs.getInt(1) else 0 }
                }
            }
            callback.accept(count)
        }
    }

    fun warpExists(player: Player, warpName: String, callback: Consumer<Boolean>) {
        plugin.launchAsync {
            val exists = withContext(dbDispatcher) {
                getConnection().prepareStatement("SELECT 1 FROM Warp WHERE owner_uuid = ? AND warp_name = ?").use { ps ->
                    ps.setString(1, player.uniqueId.toString())
                    ps.setString(2, warpName)
                    ps.executeQuery().use { it.next() }
                }
            }
            callback.accept(exists)
        }
    }

    fun getWarpObjects(player: Player, callback: Consumer<Set<Warp>>) {
        plugin.launchAsync {
            val warps = withContext(dbDispatcher) {
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
                            } else {
                                null
                            }
                        }.toSet()
                    }
                }
            }

            Bukkit.getScheduler().runTask(plugin, Runnable {
                callback.accept(warps)
            })
        }
    }

    fun isWorldBlocked(worldName: String, callback: Consumer<Boolean>) {
        plugin.launchAsync {
            val blocked = withContext(dbDispatcher) {
                getConnection().prepareStatement("SELECT is_blocked FROM World WHERE name = ?").use { ps ->
                    ps.setString(1, worldName)
                    ps.executeQuery().use { rs -> if (rs.next()) rs.getInt("is_blocked") == 1 else false }
                }
            }
            callback.accept(blocked)
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

    fun getTpaRequest(from: Player, to: Player, callback: Consumer<TpaRequest?>) {
        plugin.launchAsync {
            val request = withContext(dbDispatcher) {
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
            callback.accept(request)
        }
    }

    fun getIncomingTpaRequest(receiver: Player, callback: Consumer<TpaRequest?>) {
        plugin.launchAsync {
            val data = withContext(dbDispatcher) {
                getConnection().prepareStatement("SELECT sender_uuid, is_tpa FROM Teleport WHERE receiver_uuid = ? LIMIT 1").use { ps ->
                    ps.setString(1, receiver.uniqueId.toString())
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            val senderUUID = UUID.fromString(rs.getString("sender_uuid"))
                            val type = if (rs.getInt("is_tpa") == 1) TpaType.TPA else TpaType.TPA_HERE
                            Pair(senderUUID, type) // Devolvemos un par con la info
                        } else {
                            null
                        }
                    }
                }
            }

            Bukkit.getScheduler().runTask(plugin, Runnable {
                if (data != null) {
                    val senderUUID = data.first
                    val type = data.second
                    val sender = Bukkit.getPlayer(senderUUID)

                    if (sender?.isOnline == true) {
                        callback.accept(TpaRequest(sender, receiver, type))
                    } else {
                        deleteTeleportRequest(senderUUID.toString(), receiver.uniqueId.toString())
                        callback.accept(null)
                    }
                } else {
                    callback.accept(null)
                }
            })
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

    fun loadInventoryBytes(inventoryId: String, callback: Consumer<ByteArray?>) {
        plugin.launchAsync {
            val data = withContext(dbDispatcher) {
                getConnection().prepareStatement("SELECT data FROM Inventory WHERE inventory_id = ?").use { ps ->
                    ps.setString(1, inventoryId)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) rs.getBytes("data") else null
                    }
                }
            }
            callback.accept(data)
        }
    }
}
