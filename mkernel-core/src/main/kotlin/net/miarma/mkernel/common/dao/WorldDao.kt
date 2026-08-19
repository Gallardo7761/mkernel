package net.miarma.mkernel.common.dao

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.service.impl.DatabaseService
import org.bukkit.Bukkit
import org.bukkit.World
import java.sql.Connection
import java.util.concurrent.ConcurrentHashMap

@Singleton
class WorldDao @Inject constructor(private val dbService: DatabaseService) {
    private val blockedWorldsCache = ConcurrentHashMap.newKeySet<String>()

    fun isWorldBlockedCached(worldName: String): Boolean {
        return blockedWorldsCache.contains(worldName)
    }

    suspend fun loadBlockedWorldsCache() {
        val blocked = getBlockedWorlds()
        blockedWorldsCache.clear()
        blockedWorldsCache.addAll(blocked)
    }

    suspend fun ensureWorldExists(conn: Connection, world: World) {
        dbService.withConnection {
            conn.prepareStatement("INSERT OR IGNORE INTO World (name, is_blocked) VALUES (?, 0)").use { ps ->
                ps.setString(1, world.name)
                ps.executeUpdate()
            }
        }
    }

    suspend fun createWorld(world: World) {
        dbService.withConnection { conn ->
            ensureWorldExists(conn, world)
        }
    }

    suspend fun getBlockedWorlds(): List<String> = dbService.withConnection { conn ->
        conn.prepareStatement("SELECT name FROM World WHERE is_blocked = 1").use { ps ->
            ps.executeQuery().use { rs ->
                generateSequence { if (rs.next()) rs.getString("name") else null }.toList()
            }
        }
    }

    suspend fun isWorldBlocked(worldName: String): Boolean = dbService.withConnection { conn ->
        conn.prepareStatement("SELECT is_blocked FROM World WHERE name = ?").use { ps ->
            ps.setString(1, worldName)
            ps.executeQuery().use { rs -> if (rs.next()) rs.getInt("is_blocked") == 1 else false }
        }
    }

    suspend fun setWorldBlocked(worldName: String, blocked: Boolean) {
        dbService.withConnection { conn ->
            Bukkit.getWorld(worldName)?.let { ensureWorldExists(conn, it) }
            conn.prepareStatement("UPDATE World SET is_blocked = ? WHERE name = ?").use { ps ->
                ps.setInt(1, if (blocked) 1 else 0)
                ps.setString(2, worldName)
                ps.executeUpdate()
            }
        }
        if (blocked) blockedWorldsCache.add(worldName) else blockedWorldsCache.remove(worldName)
    }
}