package es.killobyte.nucleo.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import es.killobyte.nucleo.api.annotation.LoaderPriority
import es.killobyte.nucleo.api.common.IService
import es.killobyte.nucleo.api.model.Season
import es.killobyte.nucleo.common.config.ConfigKeys.Settings.Environment
import es.killobyte.nucleo.common.environment.TemperatureLevel
import org.bukkit.World
import org.bukkit.entity.Player
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Singleton
@LoaderPriority(LoaderPriority.LOWEST)
class EnvironmentService @Inject constructor(private val configService: ConfigService) : IService {

    private val bodyTemperatures = ConcurrentHashMap<UUID, Double>()

    override fun onDisable() {
        bodyTemperatures.clear()
    }

    fun getBiomeKey(player: Player): String {
        return player.location.block.biome.key.key.lowercase()
    }

    fun getSeason(): Season = Season.of(LocalDate.now().month)

    fun getAmbientTemperature(player: Player): Double {
        val block = player.location.block
        val world = player.world

        var temp = when (world.environment) {
            World.Environment.NETHER -> configService.getDouble(Environment.NETHER_BASE, 45.0)
            World.Environment.THE_END -> configService.getDouble(Environment.END_BASE, -15.0)
            else -> {
                val month = LocalDate.now().month.name.lowercase()
                val base = configService.getDouble("${Environment.MONTHLY_BASE}.$month", 18.0)

                val biomeOverride = configService.getDouble("${Environment.BIOME_OVERRIDES}.${getBiomeKey(player)}", Double.NaN)
                val biome = if (!biomeOverride.isNaN()) biomeOverride else
                    (block.temperature - configService.getDouble(Environment.BIOME_NEUTRAL, 0.8)) *
                        configService.getDouble(Environment.BIOME_FACTOR, 10.0)

                val seaLevel = configService.getInt(Environment.ALTITUDE_SEA_LEVEL, 64)
                val altitude = if (block.y > seaLevel)
                    -(block.y - seaLevel) / configService.getDouble(Environment.ALTITUDE_BLOCKS_PER_DEGREE, 15.0) else 0.0

                val exposure = block.lightFromSky / 15.0
                val night = if (world.time in 13000..23000) configService.getDouble(Environment.NIGHT, -6.0) else 0.0
                val weather = when {
                    !world.hasStorm() -> 0.0
                    block.temperature < 0.15 -> configService.getDouble(Environment.SNOW, -6.0)
                    else -> configService.getDouble(Environment.RAIN, -4.0)
                }

                base + biome + altitude + (night + weather) * exposure
            }
        }

        temp += block.lightFromBlocks * configService.getDouble(Environment.BLOCK_LIGHT_FACTOR, 0.5)
        if (player.isInWater) temp += configService.getDouble(Environment.WATER, -8.0)

        return temp
    }

    fun getPerceivedTemperature(player: Player): Double {
        val ambient = getAmbientTemperature(player)
        val armor = player.equipment.armorContents.filterNotNull().map { it.type.name }

        return when (getLevel(ambient)) {
            TemperatureLevel.FREEZING, TemperatureLevel.COLD ->
                ambient + armor.count { it.startsWith("LEATHER_") } *
                    configService.getDouble(Environment.LEATHER_PER_PIECE, 2.5)
            TemperatureLevel.HOT, TemperatureLevel.SCORCHING ->
                ambient + armor.count { it.startsWith("IRON_") || it.startsWith("GOLDEN_") || it.startsWith("CHAINMAIL_") ||
                    it.startsWith("DIAMOND_") || it.startsWith("NETHERITE_") } *
                    configService.getDouble(Environment.METAL_HEAT_PER_PIECE, 1.0)
            TemperatureLevel.MILD -> ambient
        }
    }

    fun getTemperature(player: Player): Double =
        bodyTemperatures[player.uniqueId] ?: getPerceivedTemperature(player)

    fun updateBodyTemperature(player: Player): Double {
        val target = getPerceivedTemperature(player)
        val current = bodyTemperatures[player.uniqueId] ?: target
        val inertia = configService.getDouble(Environment.INERTIA, 0.15).coerceIn(0.0, 1.0)
        return (current + (target - current) * inertia).also { bodyTemperatures[player.uniqueId] = it }
    }

    fun retainBodyTemperatures(online: Set<UUID>) {
        bodyTemperatures.keys.retainAll(online)
    }

    fun clearBodyTemperatures() {
        bodyTemperatures.clear()
    }

    fun getLevel(temperature: Double): TemperatureLevel = when {
        temperature <= configService.getDouble(Environment.Thresholds.FREEZING, -2.0) -> TemperatureLevel.FREEZING
        temperature <= configService.getDouble(Environment.Thresholds.COLD, 5.0) -> TemperatureLevel.COLD
        temperature >= configService.getDouble(Environment.Thresholds.SCORCHING, 42.0) -> TemperatureLevel.SCORCHING
        temperature >= configService.getDouble(Environment.Thresholds.HOT, 35.0) -> TemperatureLevel.HOT
        else -> TemperatureLevel.MILD
    }
}
