package es.killobyte.nucleo.common.integration.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import es.killobyte.nucleo.api.common.IHook
import es.killobyte.nucleo.common.config.ConfigKeys
import es.killobyte.nucleo.common.service.impl.ConfigService
import es.killobyte.nucleo.common.service.impl.EnvironmentService
import es.killobyte.nucleo.common.service.impl.LastPositionService
import es.killobyte.nucleo.common.service.impl.MessageService
import es.killobyte.nucleo.common.service.impl.PlayerService
import es.killobyte.nucleo.util.PlayerUtil.getNickName
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import kotlin.math.roundToInt

@Singleton
class PlaceholderAPIHook @Inject constructor(
    private val playerService: PlayerService,
    private val lastPositionService: LastPositionService,
    private val environmentService: EnvironmentService,
    private val configService: ConfigService,
    private val messageService: MessageService
) : IHook {

    override val pluginName = "PlaceholderAPI"
    private var expansion: NucleoExpansion? = null

    override fun register() {
        expansion = NucleoExpansion(playerService, lastPositionService, environmentService, configService, messageService).apply { register() }
    }

    override fun unregister() {
        expansion?.unregister()
    }

    fun setPlaceholders(player: Player, text: String?): String {
        return if (text == null) "" else PlaceholderAPI.setPlaceholders(player, text)
    }

    private class NucleoExpansion(
        private val playerService: PlayerService,
        private val lastPositionService: LastPositionService,
        private val environmentService: EnvironmentService,
        private val configService: ConfigService,
        private val messageService: MessageService
    ) : PlaceholderExpansion() {

        override fun getIdentifier() = "nucleo"
        override fun getAuthor() = "Gallardo7761"
        override fun getVersion() = "1.0.0"
        override fun persist() = true

        override fun onRequest(player: OfflinePlayer?, params: String): String? {
            val onlinePlayer = player?.player ?: return ""

            return when (params.lowercase()) {
                "is_frozen" -> playerService.isFrozen(onlinePlayer).toString()
                "vanished" -> playerService.isVanished(onlinePlayer).toString()
                "can_spy" -> playerService.canSpy(onlinePlayer).toString()
                "last_position" -> {
                    val last = lastPositionService.getLastPosition(onlinePlayer)
                    last?.let { "${it.blockX}, ${it.blockY}, ${it.blockZ}" } ?: "N/A"
                }
                "nick" -> onlinePlayer.getNickName(playerService)
                "biome" -> environmentService.getBiomeKey(onlinePlayer)
                "season" -> formatSeason()
                "temperature" -> formatTemperature(onlinePlayer)
                else -> null
            }
        }

        private fun formatSeason(): String {
            val season = environmentService.getSeason().name.lowercase()
            return toLegacy(configService.getString("${ConfigKeys.Messages.Environment.SEASONS}.$season", season))
        }

        private fun formatTemperature(player: Player): String {
            val temp = environmentService.getTemperature(player)
            val level = environmentService.getLevel(temp).name.lowercase()
            val format = configService.getString("${ConfigKeys.Messages.Environment.TEMPERATURE_LEVELS}.$level", "<temp>°C")
            return toLegacy(format, temp.roundToInt().toString())
        }

        private fun toLegacy(text: String, temp: String = ""): String =
            LegacyComponentSerializer.legacyAmpersand().serialize(messageService.builder(text).tag("temp", temp).build())
    }
}
