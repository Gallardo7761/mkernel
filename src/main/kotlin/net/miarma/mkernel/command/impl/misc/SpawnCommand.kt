package net.miarma.mkernel.command.impl.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.PlayerUtil

@Singleton
class SpawnCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Spawn.NAME)) {
            withPermission(configService.getString(ConfigKeys.Commands.Spawn.PERM_BASE))
            withOptionalArguments(
                PlayerProfileArgument(configService.getString(ConfigKeys.Arguments.PLAYER))
                    .withPermission(configService.getString(ConfigKeys.Commands.Spawn.PERM_OTHERS))
            )
            withFullDescription(configService.getString(ConfigKeys.Commands.Spawn.DESC))
            playerExecutor { sender, args ->
                if (args[0] == null) {
                    sender.teleportAsync(sender.world.spawnLocation.toCenterLocation()).thenAccept { success ->
                        if (success) messageService.builder(configService.getString(ConfigKeys.Commands.Spawn.MSG_TELEPORTED)).withPrefix().send(sender)
                    }
                    return@playerExecutor
                }

                val target = PlayerUtil.fromArg(args[0])
                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.PLAYER_NOT_FOUND)).withPrefix().send(sender)
                    return@playerExecutor
                }

                target.teleportAsync(target.world.spawnLocation.toCenterLocation()).thenAccept { success ->
                    if (success) {
                        messageService.builder(configService.getString(ConfigKeys.Commands.Spawn.MSG_YOU_OTHERS)).withPrefix().tag("target", target.name).send(sender)
                        messageService.builder(configService.getString(ConfigKeys.Commands.Spawn.MSG_OTHERS_YOU)).withPrefix().forPlayer(target).tag("sender", sender.name).send(target)
                    }
                }
            }
        }
    }
}