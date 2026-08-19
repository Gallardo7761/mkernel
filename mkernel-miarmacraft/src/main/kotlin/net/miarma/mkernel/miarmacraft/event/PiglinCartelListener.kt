package net.miarma.mkernel.miarmacraft.event

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.miarmacraft.common.config.ConfigKeys
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Singleton
class PiglinCartelListener @Inject constructor(
    private val messageService: MessageService,
    private val configService: ConfigService
) : Listener {

    private val portalState = ConcurrentHashMap<UUID, Boolean>()

    @EventHandler(priority = EventPriority.LOW)
    fun onNetherPortalEnter(event: PlayerPortalEvent) {
        if (!configService.isModuleEnabled(ConfigKeys.Modules.PiglinCartel.MAIN)) return
        if (event.cause != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) return

        val fromWorld = event.from.world ?: return
        val toWorld = event.to?.world ?: return

        val isGoingToNether = fromWorld.environment == World.Environment.NORMAL && toWorld.environment == World.Environment.NETHER
        val isLeavingNether = fromWorld.environment == World.Environment.NETHER && toWorld.environment == World.Environment.NORMAL
        if (!isGoingToNether && !isLeavingNether) return

        val player = event.player
        val uuid = player.uniqueId

        val existingState = portalState[uuid]
        if (existingState != null) {
            if (!existingState) event.isCancelled = true
            return
        }

        val currency = Material.valueOf(configService.getString(ConfigKeys.Settings.PiglinCartel.CURRENCY))
        val taxAmount = configService.getInt(ConfigKeys.Settings.PiglinCartel.TAX_AMOUNT)

        if (!player.inventory.containsAtLeast(ItemStack(currency), taxAmount)) {
            event.isCancelled = true
            portalState[uuid] = false
            messageService.builder(configService.getString(ConfigKeys.Messages.PiglinCartel.MSG_NOT_ENOUGH_TRIBUTE))
                .withPrefix()
                .tag("tax", taxAmount.toString())
                .send(player)
            player.playSound(player.location, Sound.ENTITY_PIGLIN_ANGRY, 1.0f, 1.0f)
            return
        }

        player.inventory.removeItem(ItemStack(currency, taxAmount))
        portalState[uuid] = true
        messageService.builder(configService.getString(ConfigKeys.Messages.PiglinCartel.MSG_PAID))
            .withPrefix()
            .tag("tax", taxAmount.toString())
            .send(player)
        player.playSound(player.location, Sound.BLOCK_CHAIN_BREAK, 1.0f, 1.0f)
    }

    @EventHandler
    fun onPortalTeleportComplete(event: PlayerTeleportEvent) {
        if (event.cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            portalState.remove(event.player.uniqueId)
        }
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        val uuid = event.player.uniqueId
        if (!portalState.containsKey(uuid)) return
        val to = event.to
        if (to.block.type != Material.NETHER_PORTAL) {
            portalState.remove(uuid)
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        portalState.remove(event.player.uniqueId)
    }
}