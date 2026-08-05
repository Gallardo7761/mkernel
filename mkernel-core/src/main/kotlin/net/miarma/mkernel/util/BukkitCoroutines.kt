package net.miarma.mkernel.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

class BukkitDispatcher(private val plugin: JavaPlugin) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Bukkit.isPrimaryThread()) block.run()
        else Bukkit.getScheduler().runTask(plugin, block)
    }
}

suspend fun delayTicks(plugin: JavaPlugin, ticks: Long) = suspendCancellableCoroutine<Unit> { cont ->
    Bukkit.getScheduler().runTaskLater(plugin, Runnable { cont.resume(Unit) }, ticks)
}