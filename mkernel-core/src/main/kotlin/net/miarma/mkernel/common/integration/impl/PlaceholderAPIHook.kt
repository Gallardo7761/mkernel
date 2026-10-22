package net.miarma.mkernel.common.integration.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.miarma.mkernel.api.common.IHook
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.EnvironmentService
import net.miarma.mkernel.common.service.impl.LastPositionService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.service.impl.PlayerService
import net.miarma.mkernel.util.PlayerUtil.getNickName
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
    private var expansion: MKernelExpansion? = null

    override fun register() {
        expansion = MKernelExpansion(playerService, lastPositionService, environmentService, configService, messageService).apply { register() }
    }

    override fun unregister() {
        expansion?.unregister()
    }

    fun setPlaceholders(player: Player, text: String?): String {
        return if (text == null) "" else PlaceholderAPI.setPlaceholders(player, text)
    }

    private class MKernelExpansion(
        private val playerService: PlayerService,
        private val lastPositionService: LastPositionService,
        private val environmentService: EnvironmentService,
        private val configService: ConfigService,
        private val messageService: MessageService
    ) : PlaceholderExpansion() {

        override fun getIdentifier() = "mkernel"
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
