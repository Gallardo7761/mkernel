import com.google.inject.Guice
import com.google.inject.Injector
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
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

class MKernel : JavaPlugin(), CoroutineScope {

    companion object {
        lateinit var PLUGIN: MKernel
            private set

        lateinit var LOGGER: Logger
            private set
    }

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    lateinit var syncDispatcher: BukkitDispatcher
        private set

    private lateinit var injector: Injector

    override fun onLoad() {
        CommandAPI.onLoad(
            CommandAPIPaperConfig(this)
                .verboseOutput(false)
                .setNamespace("mkernel")
        )

        WorldGuardHook.registerFlag()
    }

    override fun onEnable() {
        PLUGIN = this
        LOGGER = logger

        job = Job()
        syncDispatcher = BukkitDispatcher(this)
        injector = Guice.createInjector(MKernelModule(this))

        injector.getInstance(ServiceLoader::class.java).loadAll()
        injector.getInstance(HookLoader::class.java).loadAll()
        injector.getInstance(ModuleLoader::class.java).loadAll()
        injector.getInstance(CommandHandler::class.java).registerCommands()
        injector.getInstance(RecipeLoader::class.java).loadAll()
        injector.getInstance(LocationTrackerTask::class.java).start()

        LOGGER.info("I've been enabled! :)")
    }

    override fun onDisable() {
        job.cancel()
        LOGGER.info("I've been disabled! :(")
    }

    fun launchAsync(block: suspend CoroutineScope.() -> Unit) {
        launch(Dispatchers.Default, block = block)
    }

    fun launchSync(block: suspend CoroutineScope.() -> Unit) {
        launch(syncDispatcher, block = block)
    }
}