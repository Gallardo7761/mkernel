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
    suspend fun getCrimesList(player: Player): List<Crime> {
        return dbService.withConnection { conn ->
            userDao.ensureUserExists(conn, player)
            conn.prepareStatement(
                "SELECT * FROM CrimeHistory WHERE uuid = ?"
            ).use{ ps ->
                ps.setString(1, player.uniqueId.toString())
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                Crime(
                                    id = rs.getInt("id"),
                                    crime = rs.getString("crime"),
                                    timestamp = rs.getLong("date")
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    suspend fun insertCrime(player: Player, crime: String) {
        dbService.withConnection { conn ->
            userDao.ensureUserExists(conn, player)
            conn.prepareStatement(
                "INSERT INTO CrimeHistory (uuid, crime, timestamp) VALUES (?, ?, ?)"
            ).use { ps ->
                ps.setString(1, player.uniqueId.toString())
                ps.setString(2, crime)
                ps.setLong(3, System.currentTimeMillis())
                ps.executeUpdate()
            }
        }
    }
}