package es.killobyte.nucleo.command.impl.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.annotation.RequiresModule
import es.killobyte.nucleo.api.common.ICommand
import es.killobyte.nucleo.common.config.ConfigKeys
import es.killobyte.nucleo.common.dao.HomeDao
import es.killobyte.nucleo.common.module.ModuleLoader
import es.killobyte.nucleo.common.service.impl.ConfigService
import es.killobyte.nucleo.common.service.impl.MessageService
import es.killobyte.nucleo.util.CommandUtil.checkModule

@Singleton
@RequiresModule(ConfigKeys.Modules.Teleport.MAIN)
class SetHomeCommand @Inject constructor(
    private val plugin: NucleoPlugin,
    private val configService: ConfigService,
    private val homeDao: HomeDao,
    private val messageService: MessageService,
    private val moduleLoader: ModuleLoader
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.SetHome.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Teleport.MAIN)
            withPermission(configService.getString(ConfigKeys.Commands.SetHome.PERM))
            withFullDescription(configService.getString(ConfigKeys.Commands.SetHome.DESC))
            playerExecutor { sender, _ ->
                plugin.launchAsync {
                    val loc = sender.location
                    homeDao.setHome(sender, loc)
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
}