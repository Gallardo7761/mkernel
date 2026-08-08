package net.miarma.mkernel.miarmacraft.common.task.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.common.ITask
import net.miarma.mkernel.miarmacraft.common.dao.CrimeDao
import net.miarma.mkernel.miarmacraft.common.model.Crime
import net.miarma.mkernel.miarmacraft.event.LawEnforcementListener
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

@Singleton
class LawEnforcementTask @Inject constructor(
    private val plugin: MKernel,
    private val crimeDao: CrimeDao,
    private val lawEnforcementListener: LawEnforcementListener
) : ITask {

    private var task: BukkitTask? = null

    override fun start() {
        task = object : BukkitRunnable() {
            override fun run() {
                if (Math.random() > 0.10) return

                Bukkit.getOnlinePlayers().forEach { player ->
                    plugin.launchAsync {
                        val crimes = crimeDao.getCrimes(player.uniqueId)
                        val hasPending = crimes.any { it.status == Crime.CrimeStatus.PENDING }

                        if (hasPending) {
                            plugin.launchSync {
                                lawEnforcementListener.dispatchPolice(player, "", false)
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 600L, 600L)
    }

    override fun stop() {
        task?.cancel()
        task = null
    }
}