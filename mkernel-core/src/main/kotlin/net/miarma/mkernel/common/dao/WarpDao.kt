package net.miarma.mkernel.common.dao

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.api.model.Warp
import net.miarma.mkernel.common.service.impl.DatabaseService
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Singleton
class WarpDao @Inject constructor(
    private val dbService: DatabaseService,
    private val userDao: UserDao,
    private val worldDao: WorldDao
) {

    private val warpCache = ConcurrentHashMap<UUID, MutableSet<Warp>>()

    fun getWarpCount(player: Player): Int {
        return warpCache[player.uniqueId]?.size ?: 0
    }

    fun warpExists(player: Player, warpName: String): Boolean {
        return warpCache[player.uniqueId]?.any { it.alias.equals(warpName, ignoreCase = true) } ?: false
    }

    fun getWarpObjects(player: Player): Set<Warp> {
        return warpCache[player.uniqueId] ?: emptySet()
    }

    fun loadWarps(player: Player) {
        val warps = warpCache[player.uniqueId]
        if (warps != null) {
            warpCache[player.uniqueId] = warps
        }
    }

    fun unloadWarps(player: Player) {
        warpCache.remove(player.uniqueId)
    }

    suspend fun fetchWarpsFromDb(player: Player): Set<Warp> = dbService.withConnection { conn ->
        conn.prepareStatement(
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

    suspend fun createWarp(player: Player, warpName: String, location: Location) {
        val newWarp = Warp(warpName, location.x, location.y, location.z, location.world.name)
        warpCache.computeIfAbsent(player.uniqueId) { ConcurrentHashMap.newKeySet() }.add(newWarp)

        dbService.withConnection { conn ->
            userDao.ensureUserExists(conn, player)
            worldDao.ensureWorldExists(conn, location.world)
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

    suspend fun deleteWarp(player: Player, warpName: String) {
        warpCache[player.uniqueId]?.removeIf { it.alias.equals(warpName, ignoreCase = true) }
        dbService.withConnection { conn ->
            conn.prepareStatement("DELETE FROM Warp WHERE owner_uuid = ? AND warp_name = ?").use { ps ->
                ps.setString(1, player.uniqueId.toString())
                ps.setString(2, warpName)
                ps.executeUpdate()
            }
        }
    }
}