package net.miarma.mkernel.miarmacraft.common.integration.impl

import io.github.arcaneplugins.levelledmobs.LevelledMobs
import io.github.arcaneplugins.levelledmobs.wrappers.LivingEntityWrapper
import net.miarma.mkernel.api.common.IHook
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import kotlin.math.pow
import kotlin.math.roundToInt

class LevelledMobsHook : IHook {
    override val pluginName = "LevelledMobs"
    override fun register() {}

    fun applyPoliceLevel(
        entity: LivingEntity,
        baseLevel: Int = 20,
        multiplier: Double = 1.12,
        crimeCount: Int = 1
    ) {
        if (!isInstalled()) return

        val calculatedLevel = (baseLevel * multiplier.pow((crimeCount - 1).coerceAtLeast(0))).roundToInt()
        val lmPlugin = Bukkit.getPluginManager().getPlugin("LevelledMobs") as? LevelledMobs ?: return
        val lmInterface = lmPlugin.levelInterface
        val wrapper = LivingEntityWrapper.getInstance(entity)

        lmInterface.applyLevelToMob(
            wrapper,
            calculatedLevel,
            false,
            true,
            null
        )
    }
}