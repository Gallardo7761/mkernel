package net.miarma.mkernel.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.api.annotation.RequiresModule
import net.miarma.mkernel.api.common.ICommand
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.service.impl.SequenceService
import net.miarma.mkernel.util.CommandUtil.checkModule

@Singleton
@RequiresModule(ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.SEQUENCE)
class SequenceCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val sequenceService: SequenceService,
    private val moduleLoader: ModuleLoader
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Sequence.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.SEQUENCE)
            withArguments(
                StringArgument(configService.getString(ConfigKeys.Arguments.SEQUENCE))
                    .replaceSuggestions(ArgumentSuggestions.strings { _ ->
                        sequenceService.getAllNames().toTypedArray()
                    })
            )
            withAliases(*configService.getStringList(ConfigKeys.Commands.Sequence.ALIASES).toTypedArray())
            withFullDescription(configService.getString(ConfigKeys.Commands.Sequence.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.Sequence.PERM))
            withUsage(configService.getString(ConfigKeys.Commands.Sequence.USAGE))
            playerExecutor { sender, args ->
                val sequenceName = args[0] as String
                if (!sequenceService.executeSequence(sequenceName)) {
                    messageService.builder(configService.getString(ConfigKeys.Commands.Sequence.MSG_ERROR)).withPrefix().tag("name", sequenceName).send(sender)
                    return@playerExecutor
                }
                messageService.builder(configService.getString(ConfigKeys.Commands.Sequence.MSG_SUCCESS)).withPrefix().tag("name", sequenceName).send(sender)
            }
        }
    }
}