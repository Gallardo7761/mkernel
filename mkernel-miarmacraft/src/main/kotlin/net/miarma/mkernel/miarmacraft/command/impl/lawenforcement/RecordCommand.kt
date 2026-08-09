package net.miarma.mkernel.miarmacraft.command.impl.lawenforcement

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.StringTooltip
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.arguments.TextArgument
import dev.jorel.commandapi.kotlindsl.*
import kotlinx.coroutines.future.future
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.annotation.RequiresModule
import net.miarma.mkernel.api.common.ICommand
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.miarmacraft.common.config.ConfigKeys
import net.miarma.mkernel.miarmacraft.common.dao.CrimeDao
import net.miarma.mkernel.miarmacraft.common.model.CrimeHistory
import net.miarma.mkernel.util.CommandUtil.checkModule
import net.miarma.mkernel.util.PlayerUtil
import java.text.SimpleDateFormat
import java.util.*
import net.miarma.mkernel.common.config.ConfigKeys as CoreKeys

@Singleton
@RequiresModule(ConfigKeys.Modules.LawEnforcement.MAIN)
class RecordCommand @Inject constructor(
    private val plugin: MKernel,
    private val configService: ConfigService,
    private val moduleLoader: ModuleLoader,
    private val messageService: MessageService,
    private val crimeDao: CrimeDao
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Record.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.LawEnforcement.MAIN)
            withAliases(*configService.getStringList(ConfigKeys.Commands.Record.ALIASES).toTypedArray())
            withFullDescription(configService.getString(ConfigKeys.Commands.Record.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.Record.PERM))
            withUsage(configService.getString(ConfigKeys.Commands.Record.USAGE))
            withArguments(PlayerProfileArgument(configService.getString(CoreKeys.Arguments.PLAYER)))
            playerExecutor { sender, args ->
                val target = PlayerUtil.fromArg(args[0])
                if (target == null) {
                    messageService.builder(configService.getString(CoreKeys.Messages.General.Errors.PLAYER_NOT_FOUND))
                        .withPrefix()
                        .send(sender)
                    return@playerExecutor
                }

                plugin.launchAsync {
                    val history = crimeDao.getCrimeHistory(target)

                    plugin.launchSync {
                        if (history.isEmpty()) {
                            messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Record.EMPTY))
                                .withPrefix()
                                .tag("player", target.name)
                                .send(sender)
                            return@launchSync
                        }

                        messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Record.HEADER))
                            .tag("player", target.name)
                            .send(sender)

                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        val pendingFormat = configService.getString(ConfigKeys.Messages.LawEnforcement.Record.ITEM_PENDING)
                        val paidFormat = configService.getString(ConfigKeys.Messages.LawEnforcement.Record.ITEM_PAID)
                        val forgivenFormat = configService.getString(ConfigKeys.Messages.LawEnforcement.Record.ITEM_FORGIVEN)

                        history.forEach {
                            val template = when (it.status) {
                                CrimeHistory.CrimeStatus.PENDING -> pendingFormat
                                CrimeHistory.CrimeStatus.PAID -> paidFormat
                                CrimeHistory.CrimeStatus.FORGIVEN -> forgivenFormat
                            }

                            messageService.builder(template)
                                .tag("id", it.id.toString())
                                .tag("date", dateFormat.format(it.createdAt))
                                .tag("article", it.crimeName)
                                .tag("player", target.name)
                                .send(sender)
                        }

                        messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Record.FOOTER))
                            .tag("total", history.size.toString())
                            .send(sender)
                    }
                }
            }

            subcommand(configService.getString(ConfigKeys.Commands.Record.Add.NAME)) {
                withArguments(
                    PlayerProfileArgument(configService.getString(CoreKeys.Arguments.PLAYER)),

                    TextArgument(configService.getString(ConfigKeys.Commands.Arguments.CATEGORY))
                        .replaceSuggestions(ArgumentSuggestions.stringsAsync { info ->
                            plugin.future {
                                val input = info.currentArg.lowercase().replace("\"", "")
                                crimeDao.getAllCategories()
                                    .filter { it.name.lowercase().contains(input) }
                                    .map { if (it.name.contains(" ")) "\"${it.name}\"" else it.name }
                                    .toTypedArray()
                            }
                        }),

                    TextArgument(configService.getString(ConfigKeys.Commands.Arguments.ARTICLE))
                        .replaceSuggestions(ArgumentSuggestions.stringsWithTooltipsAsync { info ->
                            plugin.future {
                                val catName = (info.previousArgs.getOrDefault(1, null) as? String)?.replace("\"", "") ?: return@future emptyArray()
                                val category = crimeDao.getCategoryByName(catName) ?: return@future emptyArray()

                                val input = info.currentArg.lowercase().replace("\"", "")
                                crimeDao.getAllArticles()
                                    .filter { it.categoryId == category.id && it.name.lowercase().contains(input) }
                                    .map {
                                        val sug = if (it.name.contains(" ")) "\"${it.name}\"" else it.name
                                        StringTooltip.ofString(sug, it.description)
                                    }.toTypedArray()
                            }
                        })
                )
                withPermission(configService.getString(ConfigKeys.Commands.Record.Add.PERM))
                withFullDescription(configService.getString(ConfigKeys.Commands.Record.Add.DESC))
                withUsage(configService.getString(ConfigKeys.Commands.Record.Add.USAGE))
                playerExecutor { sender, args ->
                    val target = PlayerUtil.fromArg(args[0])
                    if (target == null) {
                        messageService.builder(configService.getString(CoreKeys.Messages.General.Errors.PLAYER_NOT_FOUND))
                            .withPrefix()
                            .send(sender)
                        return@playerExecutor
                    }

                    val articleName = args[2] as String

                    plugin.launchAsync {
                        val article = crimeDao.getArticleByName(articleName)
                        if (article == null) {
                            messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Articles.NOT_FOUND))
                                .withPrefix().tag("id", articleName).send(sender)
                            return@launchAsync
                        }
                        crimeDao.addCrimeToHistory(target, article.id, sender)
                        messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Record.ADD_SUCCESS))
                            .withPrefix()
                            .tag("player", target.name)
                            .tag("article", article.name)
                            .send(sender)
                    }
                }
            }

            subcommand(configService.getString(ConfigKeys.Commands.Record.SetStatus.NAME)) {
                withArguments(
                    PlayerProfileArgument(configService.getString(CoreKeys.Arguments.PLAYER)),
                    StringArgument(configService.getString(ConfigKeys.Commands.Arguments.ARTICLE_OR_ALL))
                        .replaceSuggestions(ArgumentSuggestions.stringsWithTooltipsAsync { info ->
                            plugin.future {
                                val input = info.currentArg.lowercase()
                                val suggestions = mutableListOf<StringTooltip>()

                                if ("all".startsWith(input)) {
                                    suggestions.add(StringTooltip.ofString("all", "Modificar todos los antecedentes"))
                                }

                                val target = PlayerUtil.fromArg(info.previousArgs.getOrDefault(0, null))
                                if (target != null) {
                                    crimeDao.getCrimeHistory(target)
                                        .filter { it.id.toString().startsWith(input) }
                                        .forEach {
                                            val statusName = when (it.status) {
                                                CrimeHistory.CrimeStatus.PENDING -> "PENDIENTE"
                                                CrimeHistory.CrimeStatus.PAID -> "PAGADO"
                                                CrimeHistory.CrimeStatus.FORGIVEN -> "INDULTADO"
                                            }
                                            suggestions.add(StringTooltip.ofString(it.id.toString(), "[$statusName] ${it.crimeName}"))
                                        }
                                }
                                suggestions.toTypedArray()
                            }
                        }),
                    StringArgument(configService.getString(ConfigKeys.Commands.Arguments.STATUS))
                        .replaceSuggestions(ArgumentSuggestions.strings(CrimeHistory.CrimeStatus.entries.map { it.name }))
                )
                withPermission(configService.getString(ConfigKeys.Commands.Record.SetStatus.PERM))
                withFullDescription(configService.getString(ConfigKeys.Commands.Record.SetStatus.DESC))
                withUsage(configService.getString(ConfigKeys.Commands.Record.SetStatus.USAGE))
                playerExecutor { sender, args ->
                    val target = PlayerUtil.fromArg(args[0])
                    if (target == null) {
                        messageService.builder(configService.getString(CoreKeys.Messages.General.Errors.PLAYER_NOT_FOUND))
                            .withPrefix()
                            .send(sender)
                        return@playerExecutor
                    }
                    val historyIdOrAll = args[1] as String
                    val status = CrimeHistory.CrimeStatus.valueOf(args[2] as String)
                    plugin.launchAsync {
                        if (historyIdOrAll.equals("all", true)) {
                            crimeDao.setAllCrimesStatus(target, status)
                        } else {
                            val historyId = historyIdOrAll.toIntOrNull()
                            if (historyId == null) {
                                messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Record.INVALID_ARGUMENT))
                                    .withPrefix()
                                    .tag("argument", historyIdOrAll)
                                    .send(sender)
                                return@launchAsync
                            }
                            crimeDao.setCrimeStatus(historyId, status)
                        }

                        plugin.launchSync {
                            messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Record.SET_STATUS_SUCCESS))
                                .withPrefix()
                                .tag("player", target.name)
                                .tag("status", status.name)
                                .send(sender)
                        }
                    }
                }
            }
        }
    }
}