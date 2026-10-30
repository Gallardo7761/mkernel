package es.killobyte.nucleo.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.annotation.LoaderPriority
import es.killobyte.nucleo.api.common.IService
import es.killobyte.nucleo.api.model.Sequence
import es.killobyte.nucleo.common.config.ConfigKeys
import es.killobyte.nucleo.util.delayTicks
import org.bukkit.Bukkit
import java.util.*

@Singleton
@LoaderPriority(LoaderPriority.LOW)
class SequenceService @Inject constructor(
    private val configService: ConfigService,
    private val plugin: NucleoPlugin
) : IService {

    private val sequenceMap = mutableMapOf<String, Sequence>()

    override fun onEnable() {
        loadSequences()
    }

    fun loadSequences() {
        sequenceMap.clear()
        val section = configService.getSection(ConfigKeys.Settings.SEQUENCES) ?: run {
            plugin.logger.warning("'sequentialCommands' section not found")
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

        plugin.logger.info("Loaded ${sequenceMap.size} command sequences!")
    }

    fun executeSequence(name: String): Boolean {
        val sequence = sequenceMap[name.lowercase()] ?: run {
            plugin.logger.warning("The sequence '$name' is not registered")
            return false
        }

        plugin.launchSync {
            for (step in sequence.steps) {
                if (step.delayTicks > 0) {
                    delayTicks(plugin, step.delayTicks)
                }

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), step.command)
            }
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
