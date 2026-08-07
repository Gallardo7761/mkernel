package net.miarma.mkernel.miarmacraft.command.impl.lawenforcement

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
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
import net.miarma.mkernel.util.CommandUtil.checkModule
import net.miarma.mkernel.util.PlayerUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import net.miarma.mkernel.common.config.ConfigKeys as CoreKeys

@Singleton
@RequiresModule(ConfigKeys.Modules.LawEnforcement.MAIN)
class AntecedentesCommand @Inject constructor(
    private val plugin: MKernel,
    private val configService: ConfigService,
    private val moduleLoader: ModuleLoader,
    private val messageService: MessageService,
    private val crimeDao: CrimeDao
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Antecedentes.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.LawEnforcement.MAIN)
            withAliases(*configService.getStringList(ConfigKeys.Commands.Antecedentes.ALIASES).toTypedArray())
            withFullDescription(configService.getString(ConfigKeys.Commands.Antecedentes.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.Antecedentes.PERM))
            withUsage(configService.getString(ConfigKeys.Commands.Antecedentes.USAGE))
            withArguments(
                PlayerProfileArgument(configService.getString(
                    CoreKeys.Arguments.PLAYER)
                )
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
                            messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Antecedentes.EMPTY))
                                .withPrefix()
                                .tag("player", target.name)
                                .send(sender)
                            return@launchSync
                        }

                        messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Antecedentes.HEADER))
                            .tag("player", target.name)
                            .send(sender)

                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        val itemFormat = configService.getString(ConfigKeys.Messages.LawEnforcement.Antecedentes.ITEM)

                        for (crime in crimes) {
                            val formattedDate = dateFormat.format(Date(crime.timestamp))
                            messageService.builder(itemFormat)
                                .tag("id", crime.id.toString())
                                .tag("date", formattedDate)
                                .tag("crime", crime.crime)
                                .tag("count", crime.count.toString())
                                .send(sender)
                        }

                        messageService.builder(configService.getString(ConfigKeys.Messages.LawEnforcement.Antecedentes.FOOTER))
                            .tag("total", crimes.size.toString())
                            .send(sender)
                    }
                }
            }
        }
    }
}