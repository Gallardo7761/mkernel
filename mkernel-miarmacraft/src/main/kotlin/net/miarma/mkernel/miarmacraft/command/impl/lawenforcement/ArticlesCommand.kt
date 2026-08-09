package net.miarma.mkernel.miarmacraft.command.impl.lawenforcement

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.StringTooltip
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.DoubleArgument
import dev.jorel.commandapi.arguments.TextArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import kotlinx.coroutines.future.future
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.annotation.RequiresModule
import net.miarma.mkernel.api.common.ICommand
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.HookService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.miarmacraft.common.config.ConfigKeys
import net.miarma.mkernel.miarmacraft.common.dao.CrimeDao
import net.miarma.mkernel.miarmacraft.common.integration.impl.InteractiveBooksHook
import net.miarma.mkernel.util.CommandUtil.checkModule

@Singleton
@RequiresModule(ConfigKeys.Modules.LawEnforcement.MAIN)
class ArticlesCommand @Inject constructor(
    private val plugin: MKernel,
    private val configService: ConfigService,
    private val moduleLoader: ModuleLoader,
    private val messageService: MessageService,
    private val crimeDao: CrimeDao,
    private val hookService: HookService
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Articles.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.LawEnforcement.MAIN)
            withAliases(*configService.getStringList(ConfigKeys.Commands.Articles.ALIASES).toTypedArray())
            withFullDescription(configService.getString(ConfigKeys.Commands.Articles.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.Articles.PERM))
            withUsage(configService.getString(ConfigKeys.Commands.Articles.USAGE))

            subcommand(configService.getString(ConfigKeys.Commands.Articles.Add.NAME)) {
                withArguments(
                    TextArgument(configService.getString(ConfigKeys.Commands.Arguments.CATEGORY))
                        .replaceSuggestions(ArgumentSuggestions.stringsWithTooltipsAsync { info ->
                            plugin.future {
                                val input = info.currentArg.lowercase().replace("\"", "")
                                crimeDao.getAllCategories()
                                    .filter { it.name.lowercase().contains(input) }
                                    .map {
                                        val sug = if (it.name.contains(" ")) "\"${it.name}\"" else it.name
                                        StringTooltip.ofString(sug, "Categoría: ${it.id}")
                                    }.toTypedArray()
                            }
                        }),
                    TextArgument(configService.getString(ConfigKeys.Commands.Arguments.NAME)),
                    TextArgument(configService.getString(ConfigKeys.Commands.Arguments.DESC)),
                    DoubleArgument(configService.getString(ConfigKeys.Commands.Arguments.FINE))
                )
                withPermission(configService.getString(ConfigKeys.Commands.Articles.Add.PERM))
                withFullDescription(configService.getString(ConfigKeys.Commands.Articles.Add.DESC))
                withUsage(configService.getString(ConfigKeys.Commands.Articles.Add.USAGE))
                playerExecutor { sender, args ->
                    val categoryName = args[0] as String
                    val name = args[1] as String
                    val description = args[2] as String
                    val fineAmount = args[3] as Double
                    plugin.launchAsync {
                        val category = crimeDao.getCategoryByName(categoryName)
                        if (category == null) {
                            messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Categories.NOT_FOUND))
                                .withPrefix().tag("id", categoryName).send(sender)
                            return@launchAsync
                        }
                        crimeDao.createArticle(category.id, name, description, fineAmount)
                        messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Articles.ADD_SUCCESS))
                            .withPrefix().tag("name", name).send(sender)

                        hookService.getHook(InteractiveBooksHook::class.java).ifPresent { it.syncBooks() }
                    }
                }
            }

            subcommand(configService.getString(ConfigKeys.Commands.Articles.Remove.NAME)) {
                withArguments(
                    TextArgument(configService.getString(ConfigKeys.Commands.Arguments.ARTICLE))
                        .replaceSuggestions(ArgumentSuggestions.stringsWithTooltipsAsync { info ->
                            plugin.future {
                                val input = info.currentArg.lowercase().replace("\"", "")
                                val categories = crimeDao.getAllCategories().associateBy { it.id }
                                crimeDao.getAllArticles()
                                    .filter { it.name.lowercase().contains(input) }
                                    .map {
                                        val catName = categories[it.categoryId]?.name ?: "Desconocida"
                                        val sug = if (it.name.contains(" ")) "\"${it.name}\"" else it.name
                                        StringTooltip.ofString(sug, "[$catName] ${it.description}")
                                    }.toTypedArray()
                            }
                        })
                )
                withPermission(configService.getString(ConfigKeys.Commands.Articles.Remove.PERM))
                withFullDescription(configService.getString(ConfigKeys.Commands.Articles.Remove.DESC))
                withUsage(configService.getString(ConfigKeys.Commands.Articles.Remove.USAGE))
                playerExecutor { sender, args ->
                    val articleName = args[0] as String
                    plugin.launchAsync {
                        val article = crimeDao.getArticleByName(articleName)
                        if (article == null) {
                            messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Articles.NOT_FOUND))
                                .withPrefix().tag("id", articleName).send(sender)
                            return@launchAsync
                        }
                        if (crimeDao.removeArticle(article.id)) {
                            messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Articles.REMOVE_SUCCESS))
                                .withPrefix().tag("name", article.name).send(sender)

                            hookService.getHook(InteractiveBooksHook::class.java).ifPresent { it.syncBooks() }
                        } else {
                            messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Articles.REMOVE_FAIL))
                                .withPrefix().tag("name", article.name).send(sender)
                        }
                    }
                }
            }

            subcommand(configService.getString(ConfigKeys.Commands.Articles.List.NAME)) {
                withPermission(configService.getString(ConfigKeys.Commands.Articles.List.PERM))
                withFullDescription(configService.getString(ConfigKeys.Commands.Articles.List.DESC))
                withUsage(configService.getString(ConfigKeys.Commands.Articles.List.USAGE))
                playerExecutor { sender, _ ->
                    plugin.launchAsync {
                        val articles = crimeDao.getAllArticles()
                        if (articles.isEmpty()) {
                            messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Articles.LIST_EMPTY))
                                .withPrefix().send(sender)
                            return@launchAsync
                        }

                        val categories = crimeDao.getAllCategories().associateBy { it.id }

                        messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Articles.LIST_HEADER))
                            .send(sender)

                        val itemFormat = configService.getString(ConfigKeys.Messages.LawEnforcement.Articles.LIST_ITEM)

                        val groupedArticles = articles.groupBy { it.categoryId }

                        groupedArticles.forEach { (categoryId, catArticles) ->
                            val catName = categories[categoryId]?.name ?: "Categoría Desconocida"

                            messageService.builder("\n<yellow><bold>» $catName</bold></yellow>")
                                .send(sender)

                            catArticles.forEach {
                                messageService.builder(itemFormat)
                                    .tag("id", it.id.toString())
                                    .tag("name", it.name)
                                    .tag("description", it.description)
                                    .send(sender)
                            }
                        }
                    }
                }
            }

            subcommand(configService.getString(ConfigKeys.Commands.Articles.Categories.NAME)) {
                withAliases(*configService.getStringList(ConfigKeys.Commands.Articles.Categories.ALIASES).toTypedArray())
                withPermission(configService.getString(ConfigKeys.Commands.Articles.Categories.PERM))
                withFullDescription(configService.getString(ConfigKeys.Commands.Articles.Categories.DESC))
                withUsage(configService.getString(ConfigKeys.Commands.Articles.Categories.USAGE))

                subcommand(configService.getString(ConfigKeys.Commands.Articles.Categories.Add.NAME)) {
                    withArguments(TextArgument(configService.getString(ConfigKeys.Commands.Arguments.NAME)))
                    withPermission(configService.getString(ConfigKeys.Commands.Articles.Categories.Add.PERM))
                    withFullDescription(configService.getString(ConfigKeys.Commands.Articles.Categories.Add.DESC))
                    withUsage(configService.getString(ConfigKeys.Commands.Articles.Categories.Add.USAGE))
                    playerExecutor { sender, args ->
                        val name = args[0] as String
                        plugin.launchAsync {
                            crimeDao.createCategory(name)
                            messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Categories.ADD_SUCCESS))
                                .withPrefix().tag("name", name).send(sender)

                            hookService.getHook(InteractiveBooksHook::class.java).ifPresent { it.syncBooks() }
                        }
                    }
                }

                subcommand(configService.getString(ConfigKeys.Commands.Articles.Categories.Remove.NAME)) {
                    withArguments(
                        TextArgument(configService.getString(ConfigKeys.Commands.Arguments.CATEGORY))
                            .replaceSuggestions(ArgumentSuggestions.stringsWithTooltipsAsync { info ->
                                plugin.future {
                                    val input = info.currentArg.lowercase().replace("\"", "")
                                    crimeDao.getAllCategories()
                                        .filter { it.name.lowercase().contains(input) }
                                        .map {
                                            val sug = if (it.name.contains(" ")) "\"${it.name}\"" else it.name
                                            StringTooltip.ofString(sug, "ID: ${it.id}")
                                        }.toTypedArray()
                                }
                            })
                    )
                    withPermission(configService.getString(ConfigKeys.Commands.Articles.Categories.Remove.PERM))
                    withFullDescription(configService.getString(ConfigKeys.Commands.Articles.Categories.Remove.DESC))
                    withUsage(configService.getString(ConfigKeys.Commands.Articles.Categories.Remove.USAGE))
                    playerExecutor { sender, args ->
                        val categoryName = args[0] as String
                        plugin.launchAsync {
                            val category = crimeDao.getCategoryByName(categoryName)
                            if (category == null) {
                                messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Categories.NOT_FOUND))
                                    .withPrefix().tag("id", categoryName).send(sender)
                                return@launchAsync
                            }

                            if (crimeDao.removeCategory(category.id)) {
                                messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Categories.REMOVE_SUCCESS))
                                    .withPrefix().tag("name", category.name).send(sender)

                                hookService.getHook(InteractiveBooksHook::class.java).ifPresent { it.syncBooks() }
                            } else {
                                messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Categories.REMOVE_FAIL))
                                    .withPrefix().tag("name", category.name).send(sender)
                            }
                        }
                    }
                }

                subcommand(configService.getString(ConfigKeys.Commands.Articles.Categories.List.NAME)) {
                    withPermission(configService.getString(ConfigKeys.Commands.Articles.Categories.List.PERM))
                    withFullDescription(configService.getString(ConfigKeys.Commands.Articles.Categories.List.DESC))
                    withUsage(configService.getString(ConfigKeys.Commands.Articles.Categories.List.USAGE))
                    playerExecutor { sender, _ ->
                        plugin.launchAsync {
                            val categories = crimeDao.getAllCategories()
                            if (categories.isEmpty()) {
                                messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Categories.LIST_EMPTY))
                                    .withPrefix().send(sender)
                                return@launchAsync
                            }
                            messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Categories.LIST_HEADER))
                                .send(sender)
                            val itemFormat = configService.getString(ConfigKeys.Messages.LawEnforcement.Categories.LIST_ITEM)
                            categories.forEach {
                                messageService.builder(itemFormat)
                                    .tag("id", it.id.toString())
                                    .tag("name", it.name)
                                    .send(sender)
                            }
                        }
                    }
                }
            }
        }
    }
}