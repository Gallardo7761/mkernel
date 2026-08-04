package net.miarma.mkernel.command.impl.base

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.inventory.ConfigInventory
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.recipe.RecipeLoader
import net.miarma.mkernel.common.service.impl.*

@Singleton
class BaseCommand @Inject constructor(
    private val plugin: MKernel,
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val configInventory: ConfigInventory,
    private val sequenceService: SequenceService,
    private val blacklistService: BlacklistService,
    private val recipeLoader: RecipeLoader,
    private val scriptService: ScriptService,
    private val moduleLoader: ModuleLoader
) : MCommand {
    val name = plugin.pluginMeta.name
    val version = plugin.pluginMeta.version
    val authors = plugin.pluginMeta.authors

    override fun register() {
        commandAPICommand("mkernel") {
            withAliases("mk")
            withPermission(configService.getString(ConfigKeys.Commands.MKernel.PERM))
            withFullDescription(configService.getString(ConfigKeys.Commands.MKernel.DESC))
            withUsage(configService.getString(ConfigKeys.Commands.MKernel.USAGE))
            anyExecutor { sender, _ ->
                messageService.builder("<gray>$name <green>v$version <gray>by <yellow>$authors")
                    .withPrefix()
                    .send(sender)
            }
            withSubcommand(CommandAPICommand("reload").apply {
                withPermission(configService.getString(ConfigKeys.Commands.MKernel.Reload.PERM))
                withFullDescription(configService.getString(ConfigKeys.Commands.MKernel.Reload.DESC))
                withUsage(configService.getString(ConfigKeys.Commands.MKernel.Reload.USAGE))
                playerExecutor { sender, _ ->
                    plugin.launchAsync {
                        try {
                            configService.reloadAll()
                            sequenceService.loadSequences()
                            scriptService.reloadScripts()
                            plugin.launchSync {
                                blacklistService.unregisterRecipes()
                                recipeLoader.loadAll()
                                messageService.builder(configService.getString(ConfigKeys.Commands.MKernel.Reload.MSG_SUCCESS))
                                    .withPrefix()
                                    .send(sender)
                            }
                        } catch (e: Exception) {
                            plugin.launchSync {
                                messageService.builder(configService.getString(ConfigKeys.Commands.MKernel.Reload.MSG_ERROR))
                                    .withPrefix()
                                    .send(sender)
                            }
                            e.printStackTrace()
                        }
                    }
                }
            })
            withSubcommand(CommandAPICommand("config").apply {
                withPermission(configService.getString(ConfigKeys.Commands.MKernel.Config.PERM))
                withFullDescription(configService.getString(ConfigKeys.Commands.MKernel.Config.DESC))
                withUsage(configService.getString(ConfigKeys.Commands.MKernel.Config.USAGE))
                playerExecutor { sender, _ ->
                    configInventory.open(sender)
                }
            })
            register()
        }
    }
}