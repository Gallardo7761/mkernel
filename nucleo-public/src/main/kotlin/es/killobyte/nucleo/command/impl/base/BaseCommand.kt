package es.killobyte.nucleo.command.impl.base

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.common.ICommand
import es.killobyte.nucleo.common.config.ConfigKeys
import es.killobyte.nucleo.common.inventory.ConfigInventory
import es.killobyte.nucleo.common.module.ModuleLoader
import es.killobyte.nucleo.common.recipe.RecipeLoader
import es.killobyte.nucleo.common.service.impl.*

@Singleton
class BaseCommand @Inject constructor(
    private val plugin: NucleoPlugin,
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val configInventory: ConfigInventory,
    private val sequenceService: SequenceService,
    private val blacklistService: BlacklistService,
    private val recipeLoader: RecipeLoader,
    private val scriptService: ScriptService,
    private val moduleLoader: ModuleLoader
) : ICommand {
    val name = plugin.pluginMeta.name
    val version = plugin.pluginMeta.version
    val authors = plugin.pluginMeta.authors

    override fun register() {
        commandAPICommand("nucleo") {
            withAliases("nu")
            withPermission(configService.getString(ConfigKeys.Commands.Nucleo.PERM))
            withFullDescription(configService.getString(ConfigKeys.Commands.Nucleo.DESC))
            withUsage(configService.getString(ConfigKeys.Commands.Nucleo.USAGE))
            anyExecutor { sender, _ ->
                messageService.builder("<gray>$name <green>v$version <gray>by <yellow>$authors")
                    .withPrefix()
                    .send(sender)
            }
            withSubcommand(CommandAPICommand("reload").apply {
                withPermission(configService.getString(ConfigKeys.Commands.Nucleo.Reload.PERM))
                withFullDescription(configService.getString(ConfigKeys.Commands.Nucleo.Reload.DESC))
                withUsage(configService.getString(ConfigKeys.Commands.Nucleo.Reload.USAGE))
                playerExecutor { sender, _ ->
                    plugin.launchAsync {
                        try {
                            configService.reloadAll()
                            sequenceService.loadSequences()
                            scriptService.reloadScripts()
                            plugin.launchSync {
                                blacklistService.unregisterRecipes()
                                recipeLoader.loadAll()
                                messageService.builder(configService.getString(ConfigKeys.Commands.Nucleo.Reload.MSG_SUCCESS))
                                    .withPrefix()
                                    .send(sender)
                            }
                        } catch (e: Exception) {
                            plugin.launchSync {
                                messageService.builder(configService.getString(ConfigKeys.Commands.Nucleo.Reload.MSG_ERROR))
                                    .withPrefix()
                                    .send(sender)
                            }
                            e.printStackTrace()
                        }
                    }
                }
            })
            withSubcommand(CommandAPICommand("config").apply {
                withPermission(configService.getString(ConfigKeys.Commands.Nucleo.Config.PERM))
                withFullDescription(configService.getString(ConfigKeys.Commands.Nucleo.Config.DESC))
                withUsage(configService.getString(ConfigKeys.Commands.Nucleo.Config.USAGE))
                playerExecutor { sender, _ ->
                    configInventory.open(sender)
                }
            })
            register()
        }
    }
}