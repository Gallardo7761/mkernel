package net.miarma.mkernel.miarmacraft.command.impl.jobs

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.DoubleArgument
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.common.ICommand
import net.miarma.mkernel.miarmacraft.common.config.ConfigKeys
import net.miarma.mkernel.common.config.ConfigKeys as CoreKeys
import net.miarma.mkernel.common.integration.impl.BancoHook
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.HookService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.miarmacraft.common.dao.PaysheetDao
import net.miarma.mkernel.util.PlayerUtil
import kotlin.math.ceil

@Singleton
class PaysheetCommand @Inject constructor(
    private val plugin: MKernel,
    private val hookService: HookService,
    private val paysheetDao: PaysheetDao,
    private val configService: ConfigService,
    private val messageService: MessageService
) : ICommand {

    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Paysheet.NAME)) {
            withPermission(configService.getString(ConfigKeys.Commands.Paysheet.PERM))
            withFullDescription(configService.getString(ConfigKeys.Commands.Paysheet.DESC))
            withUsage(configService.getString(ConfigKeys.Commands.Paysheet.USAGE))

            playerExecutor { player, _ ->
                plugin.launchAsync {
                    val amount = paysheetDao.getRetainedMoney(player);

                    plugin.launchSync {
                        if (amount > 0.0) {
                            messageService.builder(configService.getString(ConfigKeys.Commands.Paysheet.MSG_HAS_MONEY))
                                .withPrefix()
                                .tag("amount", String.format("%.2f", amount))
                                .send(player)
                        } else {
                            messageService.builder(configService.getString(ConfigKeys.Commands.Paysheet.Receive.MSG_NO_PAYSHEET))
                                .withPrefix()
                                .send(player)
                        }
                    }
                }
            }

            subcommand(configService.getString(ConfigKeys.Commands.Paysheet.Retain.NAME)) {
                withPermission(configService.getString(ConfigKeys.Commands.Paysheet.Retain.PERM))
                withFullDescription(configService.getString(ConfigKeys.Commands.Paysheet.Retain.DESC))
                withUsage(configService.getString(ConfigKeys.Commands.Paysheet.Retain.USAGE))
                withArguments(
                    PlayerProfileArgument(configService.getString(CoreKeys.Arguments.PLAYER)),
                    DoubleArgument(configService.getString(ConfigKeys.Commands.Arguments.QUANTITY), 0.01)
                )

                anyExecutor { _, args ->
                    val target = PlayerUtil.fromArg(args[0]) ?: return@anyExecutor
                    val amount = args[1] as Double

                    plugin.launchAsync {
                        val banco = hookService.getHook(BancoHook::class.java).orElse(null) ?: return@launchAsync
                        banco.depositToServerAccount(amount)
                        paysheetDao.retainMoney(target, amount)
                    }
                }
            }

            subcommand(configService.getString(ConfigKeys.Commands.Paysheet.Receive.NAME)) {
                withPermission(configService.getString(ConfigKeys.Commands.Paysheet.Receive.PERM))
                withFullDescription(configService.getString(ConfigKeys.Commands.Paysheet.Receive.DESC))
                withUsage(configService.getString(ConfigKeys.Commands.Paysheet.Receive.USAGE))

                playerExecutor { player, _ ->
                    plugin.launchAsync {
                        val banco = hookService.getHook(BancoHook::class.java).orElse(null)
                        if (banco == null) {
                            messageService.builder(configService.getString(ConfigKeys.Commands.Paysheet.Receive.MSG_BANCO_ERROR))
                                .withPrefix()
                                .send(player)
                            return@launchAsync
                        }

                        val grossAmount = paysheetDao.getRetainedMoney(player).toLong()
                        if (grossAmount <= 0.0) {
                            messageService.builder(configService.getString(ConfigKeys.Commands.Paysheet.Receive.MSG_NO_PAYSHEET))
                                .withPrefix()
                                .send(player)
                            return@launchAsync
                        }

                        val taxPercent = configService.getDouble(ConfigKeys.Settings.Paysheet.TCP_PERCENT, 15.0)
                        val taxAmount = ceil(grossAmount * (taxPercent / 100.0)).toLong()
                        val netAmount = grossAmount - taxAmount

                        val serverAccountUuid = banco.getServerAccountUuid()

                        if (banco.withdraw(serverAccountUuid, grossAmount.toDouble())) {
                            banco.deposit(player.uniqueId, netAmount.toDouble())
                            banco.depositToServerAccount(taxAmount.toDouble())
                            paysheetDao.clearRetainedMoney(player)
                            messageService.builder(configService.getString(ConfigKeys.Commands.Paysheet.Receive.MSG_SUCCESS))
                                .withPrefix()
                                .tag("bruto", grossAmount.toString())
                                .tag("neto", netAmount.toString())
                                .tag("impuesto", taxAmount.toString())
                                .tag("percent", String.format("%.0f", taxPercent))
                                .send(player)
                        } else {
                            messageService.builder(configService.getString(ConfigKeys.Commands.Paysheet.Receive.MSG_NO_MONEY_IN_SERVER_ACCOUNT))
                                .withPrefix()
                                .send(player)
                        }
                    }
                }
            }
        }
    }
}