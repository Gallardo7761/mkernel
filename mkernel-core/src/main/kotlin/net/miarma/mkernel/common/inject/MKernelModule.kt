package net.miarma.mkernel.common.inject

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.model.ModuleDef
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.dao.*
import org.bukkit.Material

class MKernelModule(private val plugin: MKernel) : AbstractModule() {

    override fun configure() {
        bind(MKernel::class.java).toInstance(plugin)
        bind(UserDao::class.java)
        bind(WorldDao::class.java)
        bind(HomeDao::class.java)
        bind(WarpDao::class.java)
        bind(InventoryDao::class.java)
        bind(TeleportDao::class.java)
        bind(ShopDao::class.java)

        val moduleBinder = Multibinder.newSetBinder(binder(), ModuleDef::class.java)

        moduleBinder.addBinding().toInstance(
            ModuleDef(
                "core", Material.ENDER_CHEST, listOf(
                    ConfigKeys.Modules.Core.Commands.DISPOSAL,
                    ConfigKeys.Modules.Core.Commands.GLOBAL_CHEST,
                    ConfigKeys.Modules.Core.Commands.PAY_XP,
                    ConfigKeys.Modules.Core.Commands.SEND_COORDS,
                    ConfigKeys.Modules.Core.Commands.NICK
                )
            )
        )
        moduleBinder.addBinding().toInstance(
            ModuleDef(
                "admin", Material.NETHERITE_SWORD, listOf(
                    ConfigKeys.Modules.Admin.CHAT,
                    ConfigKeys.Modules.Admin.Commands.SPECIAL_ITEM,
                    ConfigKeys.Modules.Admin.Commands.INVSEE,
                    ConfigKeys.Modules.Admin.Commands.GMA,
                    ConfigKeys.Modules.Admin.Commands.GMSP,
                    ConfigKeys.Modules.Admin.Commands.GMC,
                    ConfigKeys.Modules.Admin.Commands.GMS,
                    ConfigKeys.Modules.Admin.Commands.HEAL,
                    ConfigKeys.Modules.Admin.Commands.OPME,
                    ConfigKeys.Modules.Admin.Commands.DEOPME,
                    ConfigKeys.Modules.Admin.Commands.SPY,
                    ConfigKeys.Modules.Admin.Commands.VANISH,
                    ConfigKeys.Modules.Admin.Commands.SEQUENCE,
                    ConfigKeys.Modules.Admin.Commands.FLYSPEED,
                    ConfigKeys.Modules.Admin.Commands.FREEZE
                )
            )
        )
        moduleBinder.addBinding().toInstance(
            ModuleDef(
                "chat", Material.WRITABLE_BOOK, listOf(
                    ConfigKeys.Modules.Chat.FORMAT,
                    ConfigKeys.Modules.Chat.MENTIONS,
                    ConfigKeys.Modules.Chat.ROLEPLAY,
                    ConfigKeys.Modules.Chat.ENDERMAN_ANGER
                )
            )
        )
        moduleBinder.addBinding().toInstance(ModuleDef("shop", Material.EMERALD, emptyList()))
        moduleBinder.addBinding().toInstance(
            ModuleDef(
                "teleport", Material.ENDER_PEARL, listOf(
                    ConfigKeys.Modules.Teleport.SPAWN_AT_LOBBY
                )
            )
        )
        moduleBinder.addBinding().toInstance(
            ModuleDef(
                "player", Material.PLAYER_HEAD, listOf(
                    ConfigKeys.Modules.Player.JOIN_TITLE,
                    ConfigKeys.Modules.Player.LEAVE_TITLE,
                    ConfigKeys.Modules.Player.DEATH_TITLE,
                    ConfigKeys.Modules.Player.RECOVER_INVENTORY
                )
            )
        )
        moduleBinder.addBinding().toInstance(
            ModuleDef(
                "world", Material.GRASS_BLOCK, listOf(
                    ConfigKeys.Modules.World.HARVEST_RIGHT_CLICK,
                    ConfigKeys.Modules.World.AUTO_ITEM_REFILL,
                    ConfigKeys.Modules.World.NO_NETHER_PORTALS,
                    ConfigKeys.Modules.World.TIME_WEATHER_CONTROL
                )
            )
        )
    }
}