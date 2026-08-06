package net.miarma.mkernel.common.service.impl

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.Singleton
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.annotation.LoaderPriority
import net.miarma.mkernel.api.common.IService
import net.miarma.mkernel.common.dao.HomeDao
import net.miarma.mkernel.common.dao.WarpDao
import net.miarma.mkernel.common.dao.WorldDao
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.concurrent.Executors

@Singleton
@LoaderPriority(LoaderPriority.HIGHEST)
class DatabaseService @Inject constructor(
    private val plugin: MKernel,
    private val homeDaoProvider: Provider<HomeDao>,
    private val warpDaoProvider: Provider<WarpDao>,
    private val worldDaoProvider: Provider<WorldDao>
) : IService {

    private val dbFile = File(plugin.dataFolder, "database.db")
    private var connection: Connection? = null
    val dbDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    override fun onEnable() {
        runBlocking {
            try {
                connect()
                initTables()
                Bukkit.getWorlds().forEach { worldDaoProvider.get().createWorld(it) }
                plugin.logger.info("Database connected and tables initialized successfully!")
            } catch (e: SQLException) {
                plugin.logger.severe("Could not establish connection to SQLite!")
                e.printStackTrace()
            }
        }
    }

    override fun onDisable() {
        plugin.launchAsync {
            try {
                disconnect()
                plugin.logger.info("Database connection closed successfully.")
            } catch (e: SQLException) {
                plugin.logger.severe("Could not close database connection: ${e.message}")
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

    suspend fun getConnection(): Connection {
        if (connection?.isClosed != false) {
            connect()
        }
        return connection!!
    }

    private suspend fun initTables() = withContext(dbDispatcher) {
        val stream = plugin.getResource("init.sql")
            ?: error("init.sql not found. Things will not work!")

        val sqlScript = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val statements = sqlScript.split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val conn = getConnection()
        conn.createStatement().use { stmt ->
            for (sql in statements) {
                stmt.execute(sql)
            }
        }
    }

    fun loadPlayerData(player: Player) {
        homeDaoProvider.get().loadHome(player)
        warpDaoProvider.get().loadWarps(player)
    }

    fun unloadPlayerData(player: Player) {
        homeDaoProvider.get().unloadHome(player)
        warpDaoProvider.get().unloadWarps(player)
    }

    suspend fun <T> withConnection(block: suspend (Connection) -> T): T = withContext(dbDispatcher) {
        block(getConnection())
    }
}