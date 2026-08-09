package net.miarma.mkernel.miarmacraft.common.dao

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.dao.UserDao
import net.miarma.mkernel.common.service.impl.DatabaseService
import net.miarma.mkernel.miarmacraft.common.model.Crime
import net.miarma.mkernel.miarmacraft.common.model.CrimeCategory
import net.miarma.mkernel.miarmacraft.common.model.CrimeHistory
import org.bukkit.entity.Player
import java.util.*

@Singleton
class CrimeDao @Inject constructor(
    private val dbService: DatabaseService,
    private val userDao: UserDao
) {

    suspend fun createCategory(name: String): Long = dbService.withConnection { conn ->
        conn.prepareStatement("INSERT INTO CrimeCategory (name) VALUES (?)", 1).use { ps ->
            ps.setString(1, name)
            ps.executeUpdate()
            ps.generatedKeys.use { rs ->
                if (rs.next()) rs.getLong(1) else -1
            }
        }
    }

    suspend fun removeCategory(id: Int): Boolean = dbService.withConnection { conn ->
        conn.prepareStatement("DELETE FROM CrimeCategory WHERE id = ?").use { ps ->
            ps.setInt(1, id)
            ps.executeUpdate() > 0
        }
    }

    suspend fun getCategory(id: Int): CrimeCategory? = dbService.withConnection { conn ->
        conn.prepareStatement("SELECT id, name FROM CrimeCategory WHERE id = ?").use { ps ->
            ps.setInt(1, id)
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    CrimeCategory(
                        id = rs.getInt("id"),
                        name = rs.getString("name")
                    )
                } else null
            }
        }
    }

    suspend fun getAllCategories(): List<CrimeCategory> = dbService.withConnection { conn ->
        val categories = mutableListOf<CrimeCategory>()
        conn.prepareStatement("SELECT id, name FROM CrimeCategory").use { ps ->
            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    categories.add(
                        CrimeCategory(
                            id = rs.getInt("id"),
                            name = rs.getString("name")
                        )
                    )
                }
            }
        }
        categories
    }

    suspend fun createArticle(categoryId: Int, name: String, description: String, fineAmount: Double): Long = dbService.withConnection { conn ->
        conn.prepareStatement("INSERT INTO Crime (category_id, name, description, fine_amount) VALUES (?, ?, ?, ?)", 1).use { ps ->
            ps.setInt(1, categoryId)
            ps.setString(2, name)
            ps.setString(3, description)
            ps.setDouble(4, fineAmount)
            ps.executeUpdate()
            ps.generatedKeys.use { rs ->
                if (rs.next()) rs.getLong(1) else -1
            }
        }
    }

    suspend fun removeArticle(id: Int): Boolean = dbService.withConnection { conn ->
        conn.prepareStatement("DELETE FROM Crime WHERE id = ?").use { ps ->
            ps.setInt(1, id)
            ps.executeUpdate() > 0
        }
    }

    suspend fun getArticle(id: Int): Crime? = dbService.withConnection { conn ->
        conn.prepareStatement("SELECT id, category_id, name, description, fine_amount, created_at FROM Crime WHERE id = ?").use { ps ->
            ps.setInt(1, id)
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    Crime(
                        id = rs.getInt("id"),
                        categoryId = rs.getInt("category_id"),
                        name = rs.getString("name"),
                        description = rs.getString("description"),
                        fineAmount = rs.getDouble("fine_amount"),
                        createdAt = rs.getTimestamp("created_at")
                    )
                } else null
            }
        }
    }

    suspend fun getAllArticles(): List<Crime> = dbService.withConnection { conn ->
        val articles = mutableListOf<Crime>()
        conn.prepareStatement("SELECT id, category_id, name, description, fine_amount, created_at FROM Crime").use { ps ->
            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    articles.add(
                        Crime(
                            id = rs.getInt("id"),
                            categoryId = rs.getInt("category_id"),
                            name = rs.getString("name"),
                            description = rs.getString("description"),
                            fineAmount = rs.getDouble("fine_amount"),
                            createdAt = rs.getTimestamp("created_at")
                        )
                    )
                }
            }
        }
        articles
    }

    suspend fun addCrimeToHistory(user: Player, crimeId: Int, officer: Player): Long = dbService.withConnection { conn ->
        userDao.ensureUserExists(conn, user)
        userDao.ensureUserExists(conn, officer)
        conn.prepareStatement("INSERT INTO CrimeHistory (user_uuid, crime_id, officer_uuid) VALUES (?, ?, ?)", 1).use { ps ->
            ps.setString(1, user.uniqueId.toString())
            ps.setInt(2, crimeId)
            ps.setString(3, officer.uniqueId.toString())
            ps.executeUpdate()
            ps.generatedKeys.use { rs ->
                if (rs.next()) rs.getLong(1) else -1
            }
        }
    }

    suspend fun getCrimeHistory(user: Player): List<CrimeHistory> = dbService.withConnection { conn ->
        val history = mutableListOf<CrimeHistory>()
        val sql = """
            SELECT ch.id, ch.user_uuid, ch.crime_id, ch.officer_uuid, ch.created_at, ch.status, 
                   c.name as crime_name, c.description as crime_description, cc.name as category_name
            FROM CrimeHistory ch
            JOIN Crime c ON ch.crime_id = c.id
            JOIN CrimeCategory cc ON c.category_id = cc.id
            WHERE ch.user_uuid = ?
        """.trimIndent()
        conn.prepareStatement(sql).use { ps ->
            ps.setString(1, user.uniqueId.toString())
            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    history.add(
                        CrimeHistory(
                            id = rs.getInt("id"),
                            userUuid = UUID.fromString(rs.getString("user_uuid")),
                            crimeId = rs.getInt("crime_id"),
                            officerUuid = UUID.fromString(rs.getString("officer_uuid")),
                            createdAt = rs.getTimestamp("created_at"),
                            status = CrimeHistory.CrimeStatus.valueOf(rs.getString("status")),
                            crimeName = rs.getString("crime_name"),
                            crimeDescription = rs.getString("crime_description"),
                            categoryName = rs.getString("category_name")
                        )
                    )
                }
            }
        }
        history
    }

    suspend fun hasPendingCrimes(user: Player): Boolean = dbService.withConnection { conn ->
        conn.prepareStatement("SELECT 1 FROM CrimeHistory WHERE user_uuid = ? AND status = 'PENDING' LIMIT 1").use { ps ->
            ps.setString(1, user.uniqueId.toString())
            ps.executeQuery().use { it.next() }
        }
    }

    suspend fun setCrimeStatus(historyId: Int, status: CrimeHistory.CrimeStatus): Boolean = dbService.withConnection { conn ->
        conn.prepareStatement("UPDATE CrimeHistory SET status = ? WHERE id = ?").use { ps ->
            ps.setString(1, status.name)
            ps.setInt(2, historyId)
            ps.executeUpdate() > 0
        }
    }

    suspend fun setAllCrimesStatus(user: Player, status: CrimeHistory.CrimeStatus): Int = dbService.withConnection { conn ->
        conn.prepareStatement("UPDATE CrimeHistory SET status = ? WHERE user_uuid = ?").use { ps ->
            ps.setString(1, status.name)
            ps.setString(2, user.uniqueId.toString())
            ps.executeUpdate()
        }
    }
    
    suspend fun setCrimeTypeStatus(user: Player, crimeId: Int, status: CrimeHistory.CrimeStatus): Int = dbService.withConnection { conn ->
        conn.prepareStatement("UPDATE CrimeHistory SET status = ? WHERE user_uuid = ? AND crime_id = ?").use { ps ->
            ps.setString(1, status.name)
            ps.setString(2, user.uniqueId.toString())
            ps.setInt(3, crimeId)
            ps.executeUpdate()
        }
    }

    suspend fun getCategoryByName(name: String): CrimeCategory? = dbService.withConnection { conn ->
        conn.prepareStatement("SELECT id, name FROM CrimeCategory WHERE name = ?").use { ps ->
            ps.setString(1, name)
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    CrimeCategory(
                        id = rs.getInt("id"),
                        name = rs.getString("name")
                    )
                } else null
            }
        }
    }

    suspend fun getArticleByName(name: String): Crime? = dbService.withConnection { conn ->
        conn.prepareStatement("SELECT id, category_id, name, description, fine_amount, created_at FROM Crime WHERE name = ?").use { ps ->
            ps.setString(1, name)
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    Crime(
                        id = rs.getInt("id"),
                        categoryId = rs.getInt("category_id"),
                        name = rs.getString("name"),
                        description = rs.getString("description"),
                        fineAmount = rs.getDouble("fine_amount"),
                        createdAt = rs.getTimestamp("created_at")
                    )
                } else null
            }
        }
    }
}