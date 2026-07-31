package net.miarma.mkernel.command.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.PlayerUtil

@Singleton
class SpawnCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) {
    fun register() {
        commandAPICommand(configService.getString("commands.spawn.name")) {
            withPermission(configService.getString("commands.spawn.permissions.base"))
            withOptionalArguments(
                PlayerProfileArgument(configService.getString("arguments.player"))
                    .withPermission(configService.getString("commands.spawn.permissions.others"))
            )
            withFullDescription(configService.getString("commands.spawn.description"))
            playerExecutor { sender, args ->
                if (args[0] == null) {
                    sender.teleportAsync(sender.world.spawnLocation.toCenterLocation()).thenAccept { success ->
                        if (success) messageService.builder(configService.getString("commands.spawn.messages.teleported")).withPrefix().send(sender)
                    }
                    return@playerExecutor
                }

                val target = PlayerUtil.fromArg(args[0])
                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString("language.errors.playerNotFound")).withPrefix().send(sender)
                    return@playerExecutor
                }

                target.teleportAsync(target.world.spawnLocation.toCenterLocation()).thenAccept { success ->
                    if (success) {
                        messageService.builder(configService.getString("commands.spawn.messages.spawnYouOthers")).withPrefix().tag("target", target.name).send(sender)
                        messageService.builder(configService.getString("commands.spawn.messages.spawnOthersYou")).withPrefix().forPlayer(target).tag("sender", sender.name).send(target)
                    }
                }
            }
        }
    }
}