package net.miarma.mkernel.event

import com.google.inject.Inject
import com.google.inject.Singleton
import net.kyori.adventure.title.Title
import net.miarma.mkernel.common.service.impl.*
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.time.Duration

@Singleton
class PlayerConnectionListener @Inject constructor(
    private val configService: ConfigService,
    private val databaseService: DatabaseService,
    private val messageService: MessageService,
    private val playerService: PlayerService,
    private val lastPositionService: LastPositionService
) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        if (configService.isModuleEnabled("spawnAtLobby")) {
            val lobby = configService.getLobbyWorld()
            lobby.toLocation().world?.let { player.teleportAsync(lobby.toLocation()) }
        }

        if (configService.isModuleEnabled("joinTitle")) {
            val rawTitleTemplate = configService.getString("language.titles.titleFormat")
            val rawSubtitle = configService.getString("language.titles.subtitles.join")
            val times = Title.Times.times(Duration.ofMillis(1500), Duration.ofMillis(1500), Duration.ofMillis(1500))

            Bukkit.getOnlinePlayers().forEach { p ->
                val mainTitle = messageService.builder(rawTitleTemplate).tag("player", player.name).build()
                val subTitle = messageService.builder(rawSubtitle).build()
                p.showTitle(Title.title(mainTitle, subTitle, times))
            }
        }

        playerService.setVanished(player, false)
        playerService.setSpy(player, false)
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        val player = event.player

        if (configService.isModuleEnabled("leaveTitle")) {
            val rawTitleTemplate = configService.getString("language.titles.titleFormat")
            val rawSubtitle = configService.getString("language.titles.subtitles.leave")
            val times = Title.Times.times(Duration.ofMillis(1500), Duration.ofMillis(1500), Duration.ofMillis(1500))

            Bukkit.getOnlinePlayers().forEach { p ->
                val mainTitle = messageService.builder(rawTitleTemplate).tag("player", player.name).build()
                val subTitle = messageService.builder(rawSubtitle).build()
                p.showTitle(Title.title(mainTitle, subTitle, times))
            }
        }

        lastPositionService.setLastPosition(player, player.location)
    }
}
