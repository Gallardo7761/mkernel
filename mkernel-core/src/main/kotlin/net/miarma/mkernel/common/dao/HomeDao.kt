package net.miarma.mkernel.common.dao

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.service.impl.DatabaseService
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Singleton
class HomeDao @Inject constructor(
    private val dbService: DatabaseService,
    private val userDao: UserDao,
    private val worldDao: WorldDao
) {

    private val homeCache = ConcurrentHashMap<UUID, Location>()

    fun getHome(player: Player): Location? {
        return homeCache[player.uniqueId]
    }

    suspend fun loadHome(player: Player) {
        val home = fetchHomeFromDb(player) ?: return
        homeCache[player.uniqueId] = home
    }

    fun unloadHome(player: Player) {
        homeCache.remove(player.uniqueId)
    }

    suspend fun fetchHomeFromDb(player: Player): Location? = dbService.withConnection { conn ->
        conn.prepareStatement(
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

    suspend fun setHome(player: Player, location: Location) {
        homeCache[player.uniqueId] = location
        dbService.withConnection { conn ->
            userDao.ensureUserExists(conn, player)
            worldDao.ensureWorldExists(conn, location.world)
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