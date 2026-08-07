package net.miarma.mkernel.miarmacraft.common.dao

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.dao.UserDao
import net.miarma.mkernel.common.service.impl.DatabaseService
import net.miarma.mkernel.miarmacraft.common.model.Crime
import org.bukkit.entity.Player

@Singleton
class CrimeDao @Inject constructor(
    private val dbService: DatabaseService,
    private val userDao: UserDao
) {
    suspend fun insertCrime(player: Player, crime: String): Int = dbService.withConnection { conn ->
        userDao.ensureUserExists(conn, player)

        var newCount = 1

        conn.prepareStatement(
            """
            INSERT INTO CrimeHistory (uuid, crime, timestamp, count) 
            VALUES (?, ?, ?, 1)
            ON CONFLICT(uuid, crime) 
            DO UPDATE SET count = count + 1, timestamp = excluded.timestamp
            """
        ).use { ps ->
            ps.setString(1, player.uniqueId.toString())
            ps.setString(2, crime)
            ps.setLong(3, System.currentTimeMillis())
            ps.executeUpdate()
        }

        conn.prepareStatement("SELECT count FROM CrimeHistory WHERE uuid = ? AND crime = ?").use { ps ->
            ps.setString(1, player.uniqueId.toString())
            ps.setString(2, crime)
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    newCount = rs.getInt("count")
                }
            }
        }

        newCount
    }

    suspend fun getCrimes(uuid: java.util.UUID): List<Crime> = dbService.withConnection { conn ->
        val list = mutableListOf<Crime>()
        conn.prepareStatement(
            "SELECT crime_id, crime, timestamp, count FROM CrimeHistory WHERE uuid = ? ORDER BY timestamp DESC LIMIT 15"
        ).use { ps ->
            ps.setString(1, uuid.toString())
            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    list.add(
                        Crime(
                            id = rs.getInt("crime_id"),
                            crime = rs.getString("crime"),
                            timestamp = rs.getLong("timestamp"),
                            count = rs.getInt("count")
                        )
                    )
                }
            }
        }
        list
    }
}