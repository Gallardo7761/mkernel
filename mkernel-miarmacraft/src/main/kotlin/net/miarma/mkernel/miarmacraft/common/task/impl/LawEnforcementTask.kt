package net.miarma.mkernel.miarmacraft.common.task.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.common.ITask
import net.miarma.mkernel.miarmacraft.common.dao.CrimeDao
import net.miarma.mkernel.miarmacraft.common.model.CrimeHistory
import net.miarma.mkernel.miarmacraft.common.service.impl.LawEnforcementService
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

@Singleton
class LawEnforcementTask @Inject constructor(
    private val plugin: MKernel,
    private val crimeDao: CrimeDao,
    private val lawEnforcementService: LawEnforcementService
) : ITask {

    private var task: BukkitTask? = null

    override fun start() {
        task = object : BukkitRunnable() {
            override fun run() {
                if (Math.random() > 0.10) return

                Bukkit.getOnlinePlayers().forEach { player ->
                    plugin.launchAsync {
                        val crimes = crimeDao.getCrimeHistory(player)
                        val pendingCrimes = crimes.filter { it.status == CrimeHistory.CrimeStatus.PENDING }

                        if (pendingCrimes.isNotEmpty()) {
                            plugin.launchSync {
                                if (lawEnforcementService.isTownZone(player.location))
                                    lawEnforcementService.dispatchPolice(player, pendingCrimes.size)
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