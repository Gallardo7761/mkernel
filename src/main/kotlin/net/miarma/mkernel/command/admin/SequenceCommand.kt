package net.miarma.mkernel.command.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.service.impl.SequenceService

@Singleton
class SequenceCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val sequenceService: SequenceService
) {
    fun register() {
        commandAPICommand(configService.getString("commands.sequence.name")) {
            withArguments(
                StringArgument(configService.getString("arguments.sequence"))
                    .replaceSuggestions(ArgumentSuggestions.strings { _ ->
                        sequenceService.getAllNames().toTypedArray()
                    })
            )
            withAliases(*configService.getStringList("commands.sequence.aliases").toTypedArray())
            withFullDescription(configService.getString("commands.sequence.description"))
            withPermission(configService.getString("commands.sequence.permission"))
            withUsage(configService.getString("commands.sequence.usage"))
            playerExecutor { sender, args ->
                val sequenceName = args[0] as String
                if (!sequenceService.executeSequence(sequenceName)) {
                    messageService.builder(configService.getString("commands.sequence.messages.error")).withPrefix().tag("name", sequenceName).send(sender)
                    return@playerExecutor
                }
                messageService.builder(configService.getString("commands.sequence.messages.success")).withPrefix().tag("name", sequenceName).send(sender)
            }
        }
    }
}