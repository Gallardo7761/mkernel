package net.miarma.mkernel.miarmacraft.command.impl.lawenforcement

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.annotation.RequiresModule
import net.miarma.mkernel.api.common.ICommand
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.miarmacraft.common.config.ConfigKeys
import net.miarma.mkernel.miarmacraft.common.dao.CrimeDao
import net.miarma.mkernel.miarmacraft.common.model.Crime
import net.miarma.mkernel.util.CommandUtil.checkModule
import net.miarma.mkernel.util.PlayerUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

            withOptionalArguments(
                PlayerProfileArgument(configService.getString(CoreKeys.Arguments.PLAYER))
            )

            playerExecutor { sender, args ->
                val target = if (args[0] != null) PlayerUtil.fromArg(args[0]) else sender

                if (target == null) {
                    messageService.builder(configService.getString(CoreKeys.Messages.General.Errors.PLAYER_NOT_FOUND))
                        .withPrefix()
                        .send(sender)
                    return@playerExecutor
                }

                plugin.launchAsync {
                    val crimes = crimeDao.getCrimes(target.uniqueId)

                    plugin.launchSync {
                        if (crimes.isEmpty()) {
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
                        val clearedFormat = configService.getString(ConfigKeys.Messages.LawEnforcement.Record.ITEM_CLEARED)

                        for (crime in crimes) {
                            val formattedDate = dateFormat.format(Date(crime.timestamp))
                            val template = if (crime.status == Crime.CrimeStatus.PENDING) pendingFormat else clearedFormat

                            messageService.builder(template)
                                .tag("id", crime.id.toString())
                                .tag("date", formattedDate)
                                .tag("crime", crime.crime)
                                .tag("count", crime.count.toString())
                                .send(sender)
                        }

                        messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Record.FOOTER))
                            .tag("total", crimes.size.toString())
                            .tag("player", target.name)
                            .send(sender)
                    }
                }
            }

            withSubcommand(CommandAPICommand(configService.getString(ConfigKeys.Commands.Record.Add.NAME)).apply {
                withPermission(configService.getString(ConfigKeys.Commands.Record.Add.PERM))
                withUsage(configService.getString(ConfigKeys.Commands.Record.Add.USAGE))
                withFullDescription(configService.getString(ConfigKeys.Commands.Record.Add.DESC))
                withArguments(
                    PlayerProfileArgument(configService.getString(CoreKeys.Arguments.PLAYER)),
                    GreedyStringArgument("delito")
                )
                playerExecutor { sender, args ->
                    val target = PlayerUtil.fromArg(args[0]) ?: run {
                        messageService.builder(configService.getString(CoreKeys.Messages.General.Errors.PLAYER_NOT_FOUND))
                            .withPrefix()
                            .send(sender)
                        return@playerExecutor
                    }
                    val crimeName = args[1] as String

                    plugin.launchAsync {
                        crimeDao.insertCrime(target, crimeName)

                        plugin.launchSync {
                            messageService.builder(configService.getString(ConfigKeys.Commands.Record.Add.MSG_SUCCESS))
                                .withPrefix()
                                .tag("player", target.name)
                                .tag("crime", crimeName)
                                .send(sender)
                        }
                    }
                }
            })

            withSubcommand(CommandAPICommand(configService.getString(ConfigKeys.Commands.Record.Clear.NAME)).apply {
                withPermission(configService.getString(ConfigKeys.Commands.Record.Clear.PERM))
                withUsage(configService.getString(ConfigKeys.Commands.Record.Clear.USAGE))
                withFullDescription(configService.getString(ConfigKeys.Commands.Record.Clear.DESC))
                withArguments(IntegerArgument("id"))
                playerExecutor { sender, args ->
                    val crimeId = args[0] as Int

                    plugin.launchAsync {
                        val success = crimeDao.markAsCleared(crimeId, true)

                        plugin.launchSync {
                            if (success) {
                                messageService.builder(configService.getString(ConfigKeys.Commands.Record.Clear.MSG_SUCCESS))
                                    .withPrefix()
                                    .tag("id", crimeId.toString())
                                    .send(sender)
                            } else {
                                messageService.builder(configService.getString(ConfigKeys.Commands.Record.Clear.MSG_NOT_FOUND))
                                    .withPrefix()
                                    .tag("id", crimeId.toString())
                                    .send(sender)
                            }
                        }
                    }
                }
            })

            withSubcommand(CommandAPICommand(configService.getString(ConfigKeys.Commands.Record.ClearAll.NAME)).apply {
                withPermission(configService.getString(ConfigKeys.Commands.Record.ClearAll.PERM))
                withUsage(configService.getString(ConfigKeys.Commands.Record.ClearAll.USAGE))
                withFullDescription(configService.getString(ConfigKeys.Commands.Record.ClearAll.DESC))
                withArguments(PlayerProfileArgument(configService.getString(CoreKeys.Arguments.PLAYER)))
                playerExecutor { sender, args ->
                    val target = PlayerUtil.fromArg(args[0]) ?: run {
                        messageService.builder(configService.getString(CoreKeys.Messages.General.Errors.PLAYER_NOT_FOUND))
                            .withPrefix()
                            .send(sender)
                        return@playerExecutor
                    }

                    plugin.launchAsync {
                        val count = crimeDao.markAllAsCleared(target.uniqueId)

                        plugin.launchSync {
                            messageService.builder(configService.getString(ConfigKeys.Commands.Record.ClearAll.MSG_SUCCESS))
                                .withPrefix()
                                .tag("count", count.toString())
                                .tag("player", target.name)
                                .send(sender)
                        }
                    }
                }
            })

            withSubcommand(CommandAPICommand(configService.getString(ConfigKeys.Commands.Record.Pending.NAME)).apply {
                withPermission(configService.getString(ConfigKeys.Commands.Record.Pending.PERM))
                withUsage(configService.getString(ConfigKeys.Commands.Record.Pending.USAGE))
                withFullDescription(configService.getString(ConfigKeys.Commands.Record.Pending.DESC))
                withArguments(IntegerArgument("id"))
                playerExecutor { sender, args ->
                    val crimeId = args[0] as Int

                    plugin.launchAsync {
                        val success = crimeDao.markAsCleared(crimeId, false)

                        plugin.launchSync {
                            if (success) {
                                messageService.builder(configService.getString(ConfigKeys.Commands.Record.Pending.MSG_SUCCESS))
                                    .withPrefix()
                                    .tag("id", crimeId.toString())
                                    .send(sender)
                            } else {
                                messageService.builder(configService.getString(ConfigKeys.Commands.Record.Pending.MSG_NOT_FOUND))
                                    .withPrefix()
                                    .tag("id", crimeId.toString())
                                    .send(sender)
                            }
                        }
                    }
                }
            })
        }
    }
}