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
import net.miarma.mkernel.common.integration.impl.*
import net.miarma.mkernel.common.recipe.RecipeLoader
import net.miarma.mkernel.common.service.IService
import net.miarma.mkernel.common.service.impl.*
import net.miarma.mkernel.event.*
import net.miarma.mkernel.task.LocationTrackerTask
import net.miarma.mkernel.util.BukkitDispatcher
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import ovh.mythmc.banco.api.Banco
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
    private val services = mutableListOf<IService>()
    private lateinit var recipeLoader: RecipeLoader

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

        services.apply {
            add(injector.getInstance(ConfigService::class.java))
            add(injector.getInstance(DatabaseService::class.java))
            add(injector.getInstance(GlobalChestService::class.java))
            add(injector.getInstance(ScriptService::class.java))
            add(injector.getInstance(BlacklistService::class.java))
            add(injector.getInstance(SequenceService::class.java))
            add(injector.getInstance(HookService::class.java))
            add(injector.getInstance(PlayerService::class.java))
            add(injector.getInstance(LastPositionService::class.java))
            add(injector.getInstance(MessageService::class.java))
            add(injector.getInstance(TeleportService::class.java))
            add(injector.getInstance(ShopService::class.java))
        }

        val hookService = injector.getInstance(HookService::class.java)
        hookService.registerHooks(
            injector.getInstance(PlaceholderAPIHook::class.java),
            injector.getInstance(DecentHologramsHook::class.java),
            injector.getInstance(BancoHook::class.java),
            GriefPreventionHook(),
            MinepacksHook(),
            WorldGuardHook()
        )

        services.forEach { it.onEnable() }

        recipeLoader = injector.getInstance(RecipeLoader::class.java)
        recipeLoader.loadAll()

        val commandHandler = injector.getInstance(CommandHandler::class.java)
        commandHandler.registerCommands()

        registerListeners()

        injector.getInstance(LocationTrackerTask::class.java).start()

        LOGGER.info("I've been enabled! :)")
    }

    override fun onDisable() {
        job.cancel()
        services.forEach { it.onDisable() }
        LOGGER.info("I've been disabled! :(")
    }

    private fun registerListeners() {
        val pm = Bukkit.getPluginManager()
        pm.registerEvents(injector.getInstance(ChatListener::class.java), this)
        pm.registerEvents(injector.getInstance(PlayerAdvancementListener::class.java), this)
        pm.registerEvents(injector.getInstance(PlayerConnectionListener::class.java), this)
        pm.registerEvents(injector.getInstance(PlayerStatusListener::class.java), this)
        pm.registerEvents(injector.getInstance(WorldInteractionListener::class.java), this)
        pm.registerEvents(injector.getInstance(ShopListener::class.java), this)
    }

    fun launchAsync(block: suspend CoroutineScope.() -> Unit) {
        launch(Dispatchers.Default, block = block)
    }

    fun launchSync(block: suspend CoroutineScope.() -> Unit) {
        launch(syncDispatcher, block = block)
    }
}