package net.miarma.mkernel.event

import com.google.inject.Inject
import com.google.inject.Singleton
import net.kyori.adventure.title.Title
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.dao.UserDao
import net.miarma.mkernel.common.inventory.ShopBuyInventory
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.DatabaseService
import net.miarma.mkernel.common.service.impl.LastPositionService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.service.impl.PlayerService
import net.miarma.mkernel.util.PlayerUtil.getNickName
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.time.Duration

@Singleton
class PlayerConnectionListener @Inject constructor(
    private val configService: ConfigService,
    private val userDao: UserDao,
    private val databaseService: DatabaseService,
    private val messageService: MessageService,
    private val playerService: PlayerService,
    private val lastPositionService: LastPositionService
) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        player.playerListName(messageService.builder(player.getNickName(playerService)).build())

        val customJoinMessage = messageService.builder(configService.getString(ConfigKeys.Messages.Connection.JOIN))
            .tag("player", player.getNickName(playerService))
            .build()
        event.joinMessage(customJoinMessage)

        databaseService.loadPlayerData(player)

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

        val customLeaveMessage = messageService.builder(configService.getString(ConfigKeys.Messages.Connection.LEAVE))
            .tag("player", player.getNickName(playerService))
            .build()
        event.quitMessage(customLeaveMessage)

        databaseService.unloadPlayerData(player)

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
    }
}
