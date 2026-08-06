package net.miarma.mkernel.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.annotation.RequiresModule
import net.miarma.mkernel.api.common.ICommand
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.recipe.RecipeLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.CommandUtil.checkModule
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey

@Singleton
@RequiresModule(ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.SPECIAL_ITEM)
class SpecialItemCommand @Inject constructor(
    private val plugin: MKernel,
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val recipeLoader: RecipeLoader,
    private val moduleLoader: ModuleLoader
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.SpecialItem.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.SPECIAL_ITEM)
            withAliases("spi")
            withArguments(
                StringArgument(configService.getString(ConfigKeys.Arguments.ITEM))
                    .replaceSuggestions(ArgumentSuggestions.strings { _ ->
                        recipeLoader.loadedRecipes.keys.toTypedArray()
                    })
            )
            withFullDescription(configService.getString(ConfigKeys.Commands.SpecialItem.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.SpecialItem.PERM))
            withUsage(configService.getString(ConfigKeys.Commands.SpecialItem.USAGE))
            playerExecutor { sender, args ->
                val itemName = args[0] as String
                val specialItem = Bukkit.getServer().getRecipe(NamespacedKey(plugin, itemName))

                if (specialItem != null) {
                    sender.inventory.addItem(specialItem.result)
                    messageService.builder(configService.getString(ConfigKeys.Commands.SpecialItem.MSG_RECEIVED))
                        .withPrefix().tag("item", itemName).send(sender)
                } else {
                    messageService.builder(configService.getString(ConfigKeys.Messages.Misc.Errors.ITEM_NOT_FOUND))
                        .withPrefix().tag("item", itemName).send(sender)
                }
            }
        }
    }
}