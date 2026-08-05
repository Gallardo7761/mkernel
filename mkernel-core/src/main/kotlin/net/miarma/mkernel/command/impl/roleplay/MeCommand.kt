package net.miarma.mkernel.command.impl.roleplay

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.api.common.ICommand
import net.miarma.mkernel.api.annotation.RequiresModule
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.CommandUtil.checkModule
import org.bukkit.Bukkit

@Singleton
@RequiresModule(ConfigKeys.Modules.Chat.MAIN, ConfigKeys.Modules.Chat.ROLEPLAY)
class MeCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val moduleLoader: ModuleLoader
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Me.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Chat.MAIN, ConfigKeys.Modules.Chat.ROLEPLAY)
            withArguments(GreedyStringArgument(configService.getString(ConfigKeys.Arguments.MESSAGE)))
            withFullDescription(configService.getString(ConfigKeys.Commands.Me.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.Me.PERM))
            playerExecutor { sender, args ->

                val message = args[0] as String
                val builder = messageService.builder("<gold>(<player>) [Me] <gray><message>")
                    .tag("player", sender.name)
                    .tag("message", message)

                Bukkit.getServer().onlinePlayers
                    .filter { p -> p.world == sender.world && p.location.distance(sender.location) < 25 }
                    .forEach { p -> builder.send(p) }
            }
        }
    }
}