package net.miarma.mkernel.command.impl.admin

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.recipe.RecipeLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.CampfireRecipe

@Singleton
class SpecialItemCommand @Inject constructor(
    private val plugin: MKernel,
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val recipeLoader: RecipeLoader
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.SpecialItem.NAME)) {
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