package net.miarma.mkernel.command.impl.admin

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
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
        commandAPICommand(configService.getString("commands.specialitem.name")) {
            withAliases("spi")
            withArguments(
                StringArgument(configService.getString("arguments.item"))
                    .replaceSuggestions(ArgumentSuggestions.strings { _ ->
                        recipeLoader.loadedRecipes
                            .filter { it !is CampfireRecipe && it is Keyed }
                            .map { (it as Keyed).key.key }
                            .toTypedArray()
                    })
            )
            withFullDescription(configService.getString("commands.specialitem.description"))
            withPermission(configService.getString("commands.specialitem.permission"))
            withUsage(configService.getString("commands.specialitem.usage"))
            playerExecutor { sender, args ->
                val itemName = args[0] as String
                val specialItem = Bukkit.getServer().getRecipe(NamespacedKey(plugin, itemName))

                if (specialItem != null) {
                    sender.inventory.addItem(specialItem.result)
                    messageService.builder(configService.getString("commands.specialitem.messages.itemReceived"))
                        .withPrefix().tag("item", itemName).send(sender)
                } else {
                    messageService.builder(configService.getString("language.errors.itemNotFound"))
                        .withPrefix().tag("item", itemName).send(sender)
                }
            }
        }
    }
}