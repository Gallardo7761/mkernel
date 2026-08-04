package net.miarma.mkernel.command.impl.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.annotation.RequiresModule
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.DatabaseService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.CommandUtil.checkModule

@Singleton
@RequiresModule(ConfigKeys.Modules.Teleport.MAIN)
class SetHomeCommand @Inject constructor(
    private val configService: ConfigService,
    private val databaseService: DatabaseService,
    private val messageService: MessageService,
    private val moduleLoader: ModuleLoader
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.SetHome.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Teleport.MAIN)
            withPermission(configService.getString(ConfigKeys.Commands.SetHome.PERM))
            withFullDescription(configService.getString(ConfigKeys.Commands.SetHome.DESC))
            playerExecutor { sender, _ ->
                val loc = sender.location
                databaseService.setHome(sender, loc)
                messageService.builder(configService.getString(ConfigKeys.Commands.SetHome.MSG_SET))
                    .withPrefix()
                    .tag("x", loc.blockX.toString())
                    .tag("y", loc.blockY.toString())
                    .tag("z", loc.blockZ.toString())
                    .send(sender)
            }
        }
    }
}