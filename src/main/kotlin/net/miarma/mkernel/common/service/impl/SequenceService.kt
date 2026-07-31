package net.miarma.mkernel.common.service.impl

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.model.Sequence
import net.miarma.mkernel.common.service.IService
import org.bukkit.Bukkit
import java.util.*

@Singleton
class SequenceService @Inject constructor(
    private val configService: ConfigService,
    private val plugin: MKernel
) : IService {

    private val sequenceMap = mutableMapOf<String, Sequence>()

    override fun onEnable() {
        loadSequences()
    }

    fun loadSequences() {
        sequenceMap.clear()
        val section = configService.getConfig("config.yml")?.getSection("config.sequentialCommands") ?: run {
            MKernel.LOGGER.warning("'sequentialCommands' section not found")
            return
        }

        for (seqName in section.getRoutesAsStrings(false)) {
            val rawSteps = section.getMapList(seqName)
            val steps = rawSteps.mapNotNull { entry ->
                val cmd = entry["command"] as? String
                val delaySeconds = (entry["delay"] as? Number)?.toDouble() ?: 0.0
                val delayTicks = (delaySeconds * 20L).toLong()
                cmd?.let { Sequence.CommandStep(it, delayTicks) }
            }

            sequenceMap[seqName.lowercase()] = Sequence(seqName, steps)
        }

        MKernel.LOGGER.info("Loaded ${sequenceMap.size} command sequences")
    }

    fun executeSequence(name: String): Boolean {
        val sequence = sequenceMap[name.lowercase()] ?: run {
            MKernel.LOGGER.warning("The sequence '$name' is not registered")
            return false
        }

        var delay = 0L
        for (step in sequence.steps) {
            delay += step.delayTicks
            Bukkit.getScheduler().runTaskLater(
                plugin,
                Runnable { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), step.command) },
                delay
            )
        }
        return true
    }

    fun getSequence(name: String): Optional<Sequence> {
        return Optional.ofNullable(sequenceMap[name.lowercase()])
    }

    fun getAllNames(): List<String> {
        return sequenceMap.keys.toList()
    }
}
