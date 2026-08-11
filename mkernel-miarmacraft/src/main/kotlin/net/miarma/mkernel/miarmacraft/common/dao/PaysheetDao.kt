package net.miarma.mkernel.miarmacraft.common.dao

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.dao.UserDao
import net.miarma.mkernel.common.service.impl.DatabaseService
import org.bukkit.entity.Player

@Singleton
class PaysheetDao @Inject constructor(
    private val dbService: DatabaseService,
    private val userDao: UserDao
) {
    suspend fun retainMoney(player: Player, amount: Double) {
        dbService.withConnection { conn ->
            userDao.ensureUserExists(conn, player)
            val sql = """
                INSERT INTO RetainedMoney (user_uuid, amount) 
                VALUES (?, ?) 
                ON CONFLICT(user_uuid) DO UPDATE SET amount = amount + excluded.amount
            """.trimIndent()

            conn.prepareStatement(sql).use { ps ->
                ps.setString(1, player.uniqueId.toString())
                ps.setDouble(2, amount)
                ps.executeUpdate()
            }
        }
    }

    suspend fun getRetainedMoney(player: Player): Double = dbService.withConnection { conn ->
        conn.prepareStatement("SELECT amount FROM RetainedMoney WHERE user_uuid = ?").use { ps ->
            ps.setString(1, player.uniqueId.toString())
            ps.executeQuery().use { rs ->
                if (rs.next()) rs.getDouble("amount") else 0.0
            }
        }
    }

    suspend fun clearRetainedMoney(player: Player) = dbService.withConnection { conn ->
        conn.prepareStatement("UPDATE RetainedMoney SET amount = 0.0 WHERE user_uuid = ?").use { ps ->
            ps.setString(1, player.uniqueId.toString())
            ps.executeUpdate()
        }
    }
}