package net.miarma.mkernel.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.service.impl.PlayerService
import net.miarma.mkernel.util.PlayerUtil

@Singleton
class FreezeCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val playerService: PlayerService
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString("commands.freeze.name")) {
            withArguments(PlayerProfileArgument(configService.getString("arguments.player")))
            withFullDescription(configService.getString("commands.freeze.description"))
            withUsage(configService.getString("commands.freeze.usage"))
            withPermission(configService.getString("commands.freeze.permission"))
            playerExecutor { sender, args ->
                val target = PlayerUtil.fromArg(args[0]) ?: run {
                    messageService.builder(configService.getString("language.errors.playerNotFound")).withPrefix().send(sender)
                    return@playerExecutor
                }

                if (target == sender) {
                    messageService.builder(configService.getString("language.errors.cannotFreezeSelf")).withPrefix().send(sender)
                    return@playerExecutor
                }

                if (playerService.isFrozen(target)) {
                    playerService.setFrozen(target, false)
                    messageService.builder(configService.getString("commands.freeze.messages.unfrozen")).withPrefix().tag("player", target.name).send(sender)
                    messageService.builder(configService.getString("commands.freeze.messages.beenUnfrozen")).withPrefix().forPlayer(target).tag("sender", sender.name).send(target)
                } else {
                    playerService.setFrozen(target, true)
                    messageService.builder(configService.getString("commands.freeze.messages.frozen")).withPrefix().tag("player", target.name).send(sender)
                    messageService.builder(configService.getString("commands.freeze.messages.beenFrozen")).withPrefix().forPlayer(target).tag("sender", sender.name).send(target)
                }
            }
        }
    }
}