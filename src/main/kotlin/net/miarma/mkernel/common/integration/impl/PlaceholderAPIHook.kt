package net.miarma.mkernel.common.integration.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.miarma.mkernel.common.integration.IHook
import net.miarma.mkernel.common.service.impl.LastPositionService
import net.miarma.mkernel.common.service.impl.PlayerService
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

@Singleton
class PlaceholderAPIHook @Inject constructor(
    private val playerService: PlayerService,
    private val lastPositionService: LastPositionService
) : IHook {

    override val pluginName = "PlaceholderAPI"
    private var expansion: MKernelExpansion? = null

    override fun register() {
        expansion = MKernelExpansion(playerService, lastPositionService).apply { register() }
    }

    override fun unregister() {
        expansion?.unregister()
    }

    fun setPlaceholders(player: Player, text: String?): String {
        return if (text == null) "" else PlaceholderAPI.setPlaceholders(player, text)
    }

    private class MKernelExpansion(
        private val playerService: PlayerService,
        private val lastPositionService: LastPositionService
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
                else -> null
            }
        }
    }
}
