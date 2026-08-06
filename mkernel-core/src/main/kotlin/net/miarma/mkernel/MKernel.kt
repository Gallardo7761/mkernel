package net.miarma.mkernel

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Singleton
import com.google.inject.Module
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.miarma.mkernel.command.CommandHandler
import net.miarma.mkernel.common.inject.MKernelModule
import net.miarma.mkernel.common.integration.HookLoader
import net.miarma.mkernel.common.integration.impl.WorldGuardHook
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.recipe.RecipeLoader
import net.miarma.mkernel.common.service.ServiceLoader
import net.miarma.mkernel.task.LocationTrackerTask
import net.miarma.mkernel.util.BukkitDispatcher
import org.bukkit.plugin.java.JavaPlugin
import kotlin.coroutines.CoroutineContext

@Singleton
class MKernel : JavaPlugin(), CoroutineScope {
    private lateinit var job: Job
    private lateinit var syncDispatcher: BukkitDispatcher
    private lateinit var injector: Injector

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    override fun onLoad() {
        CommandAPI.onLoad(
            CommandAPIPaperConfig(this)
                .verboseOutput(false)
                .setNamespace("mkernel")
        )

        WorldGuardHook.registerFlag()
    }

    override fun onEnable() {
        job = Job()
        syncDispatcher = BukkitDispatcher(this)
        injector = initInjector()

        injector.getInstance(ServiceLoader::class.java).loadAll()
        injector.getInstance(HookLoader::class.java).loadAll()
        injector.getInstance(ModuleLoader::class.java).loadAll()
        injector.getInstance(CommandHandler::class.java).registerCommands()
        injector.getInstance(RecipeLoader::class.java).loadAll()
        injector.getInstance(LocationTrackerTask::class.java).start()

        logger.info("I've been enabled! :)")
    }

    override fun onDisable() {
        job.cancel()
        logger.info("I've been disabled! :(")
    }

    private fun initInjector(): Injector {
        val modules = mutableListOf<Module>(MKernelModule(this))

        val reflections = org.reflections.Reflections("net.miarma.mkernel")
        val subModuleClasses = reflections.getSubTypesOf(AbstractModule::class.java)

        for (clazz in subModuleClasses) {
            if (clazz == MKernelModule::class.java) continue

            runCatching {
                val instance = clazz.getDeclaredConstructor().newInstance()
                modules.add(instance)
            }.onFailure { e ->
                logger.severe("Error instantiating Guice Module ${clazz.simpleName}: ${e.message}")
            }
        }

        return Guice.createInjector(modules)
    }

    fun launchAsync(block: suspend CoroutineScope.() -> Unit) {
        launch(Dispatchers.Default, block = block)
    }

    fun launchSync(block: suspend CoroutineScope.() -> Unit) {
        launch(syncDispatcher, block = block)
    }
}