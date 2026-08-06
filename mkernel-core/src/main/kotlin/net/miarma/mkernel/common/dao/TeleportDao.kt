package net.miarma.mkernel.common.dao

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.service.impl.DatabaseService
import net.miarma.mkernel.common.teleport.TpaRequest
import net.miarma.mkernel.common.teleport.TpaType
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

@Singleton
class TeleportDao @Inject constructor(
    private val dbService: DatabaseService,
    private val userDao: UserDao
) {

    suspend fun insertTeleportRequest(senderUUID: String, receiverUUID: String, isTpa: Boolean) {
        dbService.withConnection { conn ->
            val sender = Bukkit.getPlayer(UUID.fromString(senderUUID))
            val receiver = Bukkit.getPlayer(UUID.fromString(receiverUUID))
            sender?.let { userDao.ensureUserExists(conn, it) }
            receiver?.let { userDao.ensureUserExists(conn, it) }
            conn.prepareStatement("INSERT OR REPLACE INTO Teleport (sender_uuid, receiver_uuid, is_tpa) VALUES (?, ?, ?)").use { ps ->
                ps.setString(1, senderUUID)
                ps.setString(2, receiverUUID)
                ps.setInt(3, if (isTpa) 1 else 0)
                ps.executeUpdate()
            }
        }
    }

    suspend fun deleteTeleportRequest(senderUUID: String, receiverUUID: String) {
        dbService.withConnection { conn ->
            conn.prepareStatement("DELETE FROM Teleport WHERE sender_uuid = ? AND receiver_uuid = ?").use { ps ->
                ps.setString(1, senderUUID)
                ps.setString(2, receiverUUID)
                ps.executeUpdate()
            }
        }
    }

    suspend fun getTpaRequest(from: Player, to: Player): TpaRequest? = dbService.withConnection { conn ->
        conn.prepareStatement("SELECT is_tpa FROM Teleport WHERE sender_uuid = ? AND receiver_uuid = ?").use { ps ->
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
        val data = dbService.withConnection { conn ->
            conn.prepareStatement("SELECT sender_uuid, is_tpa FROM Teleport WHERE receiver_uuid = ? LIMIT 1").use { ps ->
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

        val (senderUUID, type) = data
        val sender = Bukkit.getPlayer(senderUUID)

        return if (sender?.isOnline == true) {
            TpaRequest(sender, receiver, type)
        } else {
            deleteTeleportRequest(senderUUID.toString(), receiver.uniqueId.toString())
            null
        }
    }
}