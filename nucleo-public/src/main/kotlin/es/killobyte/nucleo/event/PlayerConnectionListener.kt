package es.killobyte.nucleo.event

import com.google.inject.Inject
import com.google.inject.Singleton
import net.kyori.adventure.title.Title
import es.killobyte.nucleo.common.config.ConfigKeys
import es.killobyte.nucleo.common.inventory.ShopBuyInventory
import es.killobyte.nucleo.common.service.impl.ConfigService
import es.killobyte.nucleo.common.service.impl.LastPositionService
import es.killobyte.nucleo.common.service.impl.MessageService
import es.killobyte.nucleo.common.service.impl.PlayerService
import es.killobyte.nucleo.common.task.impl.LocationTrackerTask
import es.killobyte.nucleo.util.PlayerUtil.getNickName
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.time.Duration

@Singleton
class PlayerConnectionListener @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val playerService: PlayerService,
    private val lastPositionService: LastPositionService,
    private val locationTrackerTask: LocationTrackerTask
) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        val joinedNick = "@${player.getNickName(playerService)}"

        Bukkit.getOnlinePlayers().forEach { onlinePlayer ->
            onlinePlayer.addCustomChatCompletions(listOf(joinedNick))
        }

        val allNicks = Bukkit.getOnlinePlayers().map { target ->
            "@${target.getNickName(playerService)}"
        }

        player.addCustomChatCompletions(allNicks)

        player.playerListName(messageService.builder(player.getNickName(playerService)).build())

        val customJoinMessage = messageService.builder(configService.getString(ConfigKeys.Messages.Connection.JOIN))
            .tag("player", player.getNickName(playerService))
            .build()
        event.joinMessage(customJoinMessage)

        playerService.loadPlayerData(player)

        if (configService.isModuleEnabled(ConfigKeys.Modules.Teleport.SPAWN_AT_LOBBY)) {
            val lobby = configService.getLobbyWorld()
            lobby.toLocation().world?.let { player.teleportAsync(lobby.toLocation()) }
        }

        if (configService.isModuleEnabled(ConfigKeys.Modules.Player.JOIN_TITLE)) {
            val rawTitleTemplate = configService.getString(ConfigKeys.Messages.General.Titles.FORMAT)
            val rawSubtitle = configService.getString(ConfigKeys.Messages.General.Titles.JOIN)
            val times = Title.Times.times(Duration.ofMillis(1500), Duration.ofMillis(1500), Duration.ofMillis(1500))

            Bukkit.getOnlinePlayers().forEach { p ->
                val mainTitle = messageService.builder(rawTitleTemplate)
                    .tag("player", player.getNickName(playerService))
                    .build()
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
        val quitNick = "@${player.getNickName(playerService)}"

        Bukkit.getOnlinePlayers().forEach { onlinePlayer ->
            onlinePlayer.removeCustomChatCompletions(listOf(quitNick))
        }

        val customLeaveMessage = messageService.builder(configService.getString(ConfigKeys.Messages.Connection.LEAVE))
            .tag("player", player.getNickName(playerService))
            .build()
        event.quitMessage(customLeaveMessage)

        playerService.unloadPlayerData(player)

        if (configService.isModuleEnabled(ConfigKeys.Modules.Player.LEAVE_TITLE)) {
            val rawTitleTemplate = configService.getString(ConfigKeys.Messages.General.Titles.FORMAT)
            val rawSubtitle = configService.getString(ConfigKeys.Messages.General.Titles.LEAVE)
            val times = Title.Times.times(Duration.ofMillis(1500), Duration.ofMillis(1500), Duration.ofMillis(1500))

            Bukkit.getOnlinePlayers().forEach { p ->
                val mainTitle = messageService.builder(rawTitleTemplate)
                    .tag("player", player.getNickName(playerService))
                    .build()
                val subTitle = messageService.builder(rawSubtitle).build()
                p.showTitle(Title.title(mainTitle, subTitle, times))
            }
        }

        ShopBuyInventory.clickCooldowns.remove(player.uniqueId)
        lastPositionService.setLastPosition(player, player.location)
        locationTrackerTask.forget(player)
    }
}
