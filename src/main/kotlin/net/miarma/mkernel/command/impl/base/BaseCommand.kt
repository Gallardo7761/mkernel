package net.miarma.mkernel.command.impl.base

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.inventory.ConfigInventory
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
    private val scriptService: ScriptService
) : MCommand {
    override fun register() {
        commandAPICommand("mkernel") {
            withAliases("mk")
            withPermission(configService.getString("commands.mkernel.permission"))
            withFullDescription(configService.getString("commands.mkernel.description"))
            withUsage(configService.getString("commands.mkernel.usage"))
            anyExecutor { sender, _ ->
                messageService.builder("<yellow>MKernel v26.8.2 by Gallardo7761</yellow>").send(sender)
            }
            withSubcommand(CommandAPICommand("reload").apply {
                withPermission(configService.getString("commands.mkernel.subcommands.reload.permission"))
                withFullDescription(configService.getString("commands.mkernel.subcommands.reload.description"))
                withUsage(configService.getString("commands.mkernel.subcommands.reload.usage"))
                playerExecutor { sender, _ ->
                    plugin.launchAsync {
                        try {
                            configService.reloadAll()
                            sequenceService.loadSequences()
                            scriptService.reloadScripts()
                            plugin.launchSync {
                                blacklistService.unregisterRecipes()
                                recipeLoader.loadAll()
                                messageService.builder(configService.getString("commands.mkernel.subcommands.reload.messages.success"))
                                    .withPrefix()
                                    .send(sender)
                            }
                        } catch (e: Exception) {
                            plugin.launchSync {
                                messageService.builder(configService.getString("commands.mkernel.subcommands.reload.messages.error"))
                                    .withPrefix()
                                    .send(sender)
                            }
                            e.printStackTrace()
                        }
                    }
                }
            })
            withSubcommand(CommandAPICommand("config").apply {
                withPermission(configService.getString("commands.mkernel.subcommands.config.permission"))
                withFullDescription(configService.getString("commands.mkernel.subcommands.config.description"))
                withUsage(configService.getString("commands.mkernel.subcommands.config.usage"))
                playerExecutor { sender, _ ->
                    configInventory.open(sender)
                }
            })
            register()
        }
    }
}