package es.killobyte.nucleo.priv.event

import com.google.inject.Inject
import com.google.inject.Singleton
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.common.config.ConfigKeys as CoreKeys
import es.killobyte.nucleo.common.service.impl.ConfigService
import es.killobyte.nucleo.common.service.impl.MessageService
import es.killobyte.nucleo.priv.common.config.ConfigKeys
import es.killobyte.nucleo.priv.common.service.impl.LawEnforcementService
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerQuitEvent

@Singleton
class PoliceChaseListener @Inject constructor(
    private val plugin: NucleoPlugin,
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val lawEnforcementService: LawEnforcementService
) : Listener {

    private fun blockedCommands(): Set<String> = setOf(
        configService.getString(CoreKeys.Commands.Back.NAME),
        configService.getString(CoreKeys.Commands.Home.NAME),
        configService.getString(CoreKeys.Commands.Tpa.NAME),
        configService.getString(CoreKeys.Commands.TpaHere.NAME),
        configService.getString(CoreKeys.Commands.TpAccept.NAME),
        configService.getString(CoreKeys.Commands.Spawn.NAME),
        configService.getString(CoreKeys.Commands.Lobby.NAME),
        configService.getString(CoreKeys.Commands.PayXp.NAME),
        configService.getString(CoreKeys.Commands.SendCoords.NAME)
    ).map { it.lowercase() }.toSet()

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    fun onCommandWhileChased(event: PlayerCommandPreprocessEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.LawEnforcement.MAIN)) return

        val player = event.player
        if (!lawEnforcementService.isChased(player.uniqueId)) return

        val cmd = event.message.removePrefix("/").trim().substringBefore(" ").lowercase()
        if (cmd in blockedCommands()) {
            event.isCancelled = true
            messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.MSG_CANNOT_DO_WHILE_CHASED))
                .withPrefix()
                .send(player)
        }
    }

    @EventHandler
    fun onQuitWhileChased(event: PlayerQuitEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.LawEnforcement.MAIN)) return

        val player = event.player
        if (!lawEnforcementService.isChased(player.uniqueId)) return

        val duration = configService.getString(ConfigKeys.Settings.LawEnforcement.TEMP_BAN_DURATION)
        val reason = configService.getString(ConfigKeys.Messages.LawEnforcement.MSG_FLEE_BAN_REASON)

        lawEnforcementService.tempBanIfNotAlready(player.name, player.uniqueId, duration, reason)
        lawEnforcementService.clearChased(player.uniqueId)
    }
}