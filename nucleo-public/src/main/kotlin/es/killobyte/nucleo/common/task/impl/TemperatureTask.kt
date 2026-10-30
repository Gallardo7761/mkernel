package es.killobyte.nucleo.common.task.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.annotation.RequiresModule
import es.killobyte.nucleo.api.common.ITask
import es.killobyte.nucleo.common.config.ConfigKeys
import es.killobyte.nucleo.common.config.ConfigKeys.Settings.Environment
import es.killobyte.nucleo.common.environment.TemperatureLevel
import es.killobyte.nucleo.common.service.impl.ConfigService
import es.killobyte.nucleo.common.service.impl.EnvironmentService
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask

@Singleton
@RequiresModule(ConfigKeys.Modules.Environment.MAIN)
class TemperatureTask @Inject constructor(
    private val plugin: NucleoPlugin,
    private val configService: ConfigService,
    private val environmentService: EnvironmentService
) : ITask {
    private var task: BukkitTask? = null
    private var period = 40L

    override fun start() {
        period = configService.getInt(Environment.PERIOD_TICKS, 40).coerceAtLeast(1).toLong()
        task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable { tick() }, period, period)
    }

    override fun stop() {
        task?.cancel()
        task = null
        environmentService.clearBodyTemperatures()
    }

    private fun tick() {
        val players = Bukkit.getOnlinePlayers()
        environmentService.retainBodyTemperatures(players.mapTo(HashSet()) { it.uniqueId })

        val effectsEnabled = configService.getBoolean(ConfigKeys.Modules.Environment.TEMPERATURE_EFFECTS, true)
        for (player in players) {
            val temp = environmentService.updateBodyTemperature(player)
            if (effectsEnabled && isAffected(player)) {
                applyEffects(player, environmentService.getLevel(temp))
            }
        }
    }

    private fun isAffected(player: Player): Boolean =
        !player.isDead && (player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.ADVENTURE)

    private fun applyEffects(player: Player, level: TemperatureLevel) {
        when (level) {
            TemperatureLevel.FREEZING -> {
                val gain = configService.getInt(Environment.Effects.FREEZE_GAIN, 20)
                player.freezeTicks = (player.freezeTicks + 2 * period.toInt() + gain)
                    .coerceAtMost(player.maxFreezeTicks + 2 * period.toInt())
            }
            TemperatureLevel.COLD -> player.addPotionEffect(effect(PotionEffectType.SLOWNESS))
            TemperatureLevel.MILD -> {}
            TemperatureLevel.HOT -> {
                if (player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) return
                player.exhaustion += configService.getFloat(Environment.Effects.HOT_EXHAUSTION, 0.5f)
            }
            TemperatureLevel.SCORCHING -> {
                if (player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) return
                player.exhaustion += configService.getFloat(Environment.Effects.SCORCHING_EXHAUSTION, 1.5f)
                player.addPotionEffect(effect(PotionEffectType.WEAKNESS))
            }
        }
    }

    private fun effect(type: PotionEffectType) =
        PotionEffect(type, period.toInt() + 20, 0, true, false, true)
}
