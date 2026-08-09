package net.miarma.mkernel.common.dao

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.service.impl.DatabaseService
import org.bukkit.entity.Player
import java.sql.Connection

@Singleton
class UserDao @Inject constructor(private val dbService: DatabaseService) {

    suspend fun ensureUserExists(conn: Connection, player: Player) {
        conn.prepareStatement("INSERT OR IGNORE INTO User (uuid, name) VALUES (?, ?)").use { ps ->
            ps.setString(1, player.uniqueId.toString())
            ps.setString(2, player.name)
            ps.executeUpdate()
        }
    }
}