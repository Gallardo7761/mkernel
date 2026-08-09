package net.miarma.mkernel.miarmacraft.command.impl.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.annotation.RequiresModule
import net.miarma.mkernel.api.common.ICommand
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.miarmacraft.common.config.ConfigKeys
import net.miarma.mkernel.util.CommandUtil.checkModule
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.EulerAngle
import java.time.Duration
import kotlin.math.sin

@Singleton
@RequiresModule(ConfigKeys.Modules.Shit.MAIN)
class SixSevenCommand @Inject constructor(
    private val plugin: MKernel,
    private val moduleLoader: ModuleLoader,
    private val configService: ConfigService,
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.SixSeven.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Shit.MAIN)
            withFullDescription(configService.getString(ConfigKeys.Commands.SixSeven.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.SixSeven.PERM))
            withUsage(configService.getString(ConfigKeys.Commands.SixSeven.USAGE))
            playerExecutor { sender, args ->
                sendSixSevenTitle(sender)
                spawnSixSevenEntity(sender)
            }
        }
    }

    private fun sendSixSevenTitle(player: Player) {
        val title = Title.title(
            Component.text("SIX SEVEEEEEN", NamedTextColor.LIGHT_PURPLE),
            Component.text("↑↓ ↑↓ ↑↓", NamedTextColor.DARK_PURPLE),
            Title.Times.times(
                Duration.ofMillis(200),
                Duration.ofSeconds(2),
                Duration.ofMillis(300)
            )
        )
        player.showTitle(title)
    }

    private fun spawnSixSevenEntity(player: Player) {
        val loc = player.location.add(player.location.direction.multiply(2.0))
        loc.y = player.location.y

        val directionToPlayer = player.eyeLocation.toVector().subtract(loc.toVector())
        loc.setDirection(directionToPlayer)

        val stand = loc.world.spawn(loc, ArmorStand::class.java) { armorStand ->
            armorStand.isVisible = true
            armorStand.setBasePlate(false)
            armorStand.setGravity(false)
            armorStand.isSmall = false
            armorStand.setArms(true)
            armorStand.isInvulnerable = true
            armorStand.equipment.setHelmet(ItemStack(Material.PIGLIN_HEAD))
            armorStand.equipment.setChestplate(ItemStack(Material.GOLDEN_CHESTPLATE))
            armorStand.equipment.setLeggings(ItemStack(Material.GOLDEN_LEGGINGS))
            armorStand.equipment.setBoots(ItemStack(Material.GOLDEN_BOOTS))
            armorStand.isMarker = true
        }

        val baseAngle = -Math.PI / 2
        val swingHalf = Math.toRadians(20.0)

        object : BukkitRunnable() {
            var tick = 0
            val maxTicks = 60
            override fun run() {
                if (tick >= maxTicks || stand.isDead) {
                    stand.remove()
                    cancel()
                    return
                }
                val swing = sin(tick * 0.5) * swingHalf
                stand.rightArmPose = EulerAngle(baseAngle + swing, 0.0, 0.0)
                stand.leftArmPose = EulerAngle(baseAngle - swing, 0.0, 0.0)
                tick++
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }
}