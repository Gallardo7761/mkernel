package net.miarma.mkernel.common.inventory

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.api.model.ModuleDef
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.Click
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.AbstractItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.window.Window
import java.io.IOException
import kotlin.math.ceil
import kotlin.math.max

@Singleton
class ConfigInventory @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val moduleLoader: ModuleLoader
) {
    private val moduleDefs = listOf(
        ModuleDef(
            "core", Material.ENDER_CHEST, listOf(
                ConfigKeys.Modules.Core.Commands.DISPOSAL,
                ConfigKeys.Modules.Core.Commands.GLOBAL_CHEST,
                ConfigKeys.Modules.Core.Commands.PAY_XP,
                ConfigKeys.Modules.Core.Commands.SEND_COORDS,
                ConfigKeys.Modules.Core.Commands.NICK
            )
        ),
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
        ),
        ModuleDef(
            "chat", Material.WRITABLE_BOOK, listOf(
                ConfigKeys.Modules.Chat.FORMAT,
                ConfigKeys.Modules.Chat.MENTIONS,
                ConfigKeys.Modules.Chat.ROLEPLAY,
                ConfigKeys.Modules.Chat.ENDERMAN_ANGER
            )
        ),
        ModuleDef("shop", Material.EMERALD, emptyList()),
        ModuleDef(
            "teleport", Material.ENDER_PEARL, listOf(
                ConfigKeys.Modules.Teleport.SPAWN_AT_LOBBY
            )
        ),
        ModuleDef(
            "player", Material.PLAYER_HEAD, listOf(
                ConfigKeys.Modules.Player.JOIN_TITLE,
                ConfigKeys.Modules.Player.LEAVE_TITLE,
                ConfigKeys.Modules.Player.DEATH_TITLE,
                ConfigKeys.Modules.Player.RECOVER_INVENTORY
            )
        ),
        ModuleDef(
            "world", Material.GRASS_BLOCK, listOf(
                ConfigKeys.Modules.World.HARVEST_RIGHT_CLICK,
                ConfigKeys.Modules.World.AUTO_ITEM_REFILL,
                ConfigKeys.Modules.World.NO_NETHER_PORTALS,
                ConfigKeys.Modules.World.TIME_WEATHER_CONTROL
            )
        )
    )

    fun open(player: Player) {
        val moduleItems = moduleDefs.map { def -> getModuleItem(def) }
        val rows = max(1, ceil(moduleItems.size / 9.0).toInt())
        val structure = Array(rows) { "x x x x x x x x x" }

        val gui = PagedGui.itemsBuilder()
            .setStructure(*structure)
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .setContent(moduleItems)
            .build()

        val title = messageService.builder(configService.getString(ConfigKeys.Messages.Inventories.CONFIG_TITLE)).build()

        Window.builder()
            .setTitle(title)
            .setUpperGui(gui)
            .setViewer(player)
            .build()
            .open()
    }

    private fun getModuleItem(def: ModuleDef): Item {
        return object : AbstractItem() {
            override fun getItemProvider(player: Player): ItemProvider {
                val enabled = moduleLoader.isModuleEnabled(def.id)

                val stateColor = configService.getString(
                    if (enabled) ConfigKeys.Messages.Inventories.CONFIG_STATE_ENABLED_COLOR
                    else ConfigKeys.Messages.Inventories.CONFIG_STATE_DISABLED_COLOR
                )
                val stateText = configService.getString(
                    if (enabled) ConfigKeys.Messages.Inventories.CONFIG_STATE_ENABLED_TEXT
                    else ConfigKeys.Messages.Inventories.CONFIG_STATE_DISABLED_TEXT
                )

                val displayName = messageService.builder(configService.getString(ConfigKeys.Messages.Inventories.CONFIG_MODULE_NAME))
                    .tag("module", def.id.replaceFirstChar { it.uppercase() })
                    .tag("state_color", stateColor)
                    .build()

                val stateLine = messageService.builder(configService.getString(ConfigKeys.Messages.Inventories.CONFIG_MODULE_LORE_STATE))
                    .tag("state", stateText)
                    .build()

                val hintLine = messageService.builder(configService.getString(ConfigKeys.Messages.Inventories.CONFIG_MODULE_LORE_HINT)).build()

                val itemStack = ItemStack(def.icon)
                val meta = itemStack.itemMeta
                if (meta != null) {
                    meta.displayName(displayName)
                    meta.lore(listOf(stateLine, hintLine))
                    itemStack.itemMeta = meta
                }
                return ItemWrapper(itemStack)
            }

            override fun handleClick(clickType: ClickType, clickPlayer: Player, click: Click) {
                clickPlayer.playSound(clickPlayer.location, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f)
                openModule(clickPlayer, def)
            }
        }
    }

    private fun openModule(player: Player, def: ModuleDef) {
        val featureItems = def.features.map { path -> getFeatureItem(path) }
        val contentRows = max(1, ceil(featureItems.size / 9.0).toInt())

        val structure = mutableListOf("b t # # # # # # #")
        repeat(contentRows) { structure.add("x x x x x x x x x") }

        val gui = PagedGui.itemsBuilder()
            .setStructure(*structure.toTypedArray())
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('b', getBackItem())
            .addIngredient('t', getModuleToggleItem(def))
            .setContent(featureItems)
            .build()

        val title = messageService.builder(configService.getString(ConfigKeys.Messages.Inventories.CONFIG_SUBTITLE))
            .tag("module", def.id.replaceFirstChar { it.uppercase() })
            .build()

        Window.builder()
            .setTitle(title)
            .setUpperGui(gui)
            .setViewer(player)
            .build()
            .open()
    }

    private fun getBackItem(): Item {
        return object : AbstractItem() {
            override fun getItemProvider(player: Player): ItemProvider {
                val itemStack = ItemStack(Material.ARROW)
                val meta = itemStack.itemMeta
                if (meta != null) {
                    meta.displayName(messageService.builder(configService.getString(ConfigKeys.Messages.Inventories.CONFIG_BACK_NAME)).build())
                    itemStack.itemMeta = meta
                }
                return ItemWrapper(itemStack)
            }

            override fun handleClick(clickType: ClickType, clickPlayer: Player, click: Click) {
                clickPlayer.playSound(clickPlayer.location, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f)
                open(clickPlayer)
            }
        }
    }

    private fun getModuleToggleItem(def: ModuleDef): Item {
        return object : AbstractItem() {
            override fun getItemProvider(player: Player): ItemProvider {
                val enabled = moduleLoader.isModuleEnabled(def.id)

                val toggleStateText = configService.getString(
                    if (enabled) ConfigKeys.Messages.Inventories.CONFIG_TOGGLE_STATE_ENABLED
                    else ConfigKeys.Messages.Inventories.CONFIG_TOGGLE_STATE_DISABLED
                )
                val actionText = configService.getString(
                    if (enabled) ConfigKeys.Messages.Inventories.CONFIG_ACTION_DISABLE
                    else ConfigKeys.Messages.Inventories.CONFIG_ACTION_ENABLE
                )

                val displayName = messageService.builder(configService.getString(ConfigKeys.Messages.Inventories.CONFIG_TOGGLE_NAME))
                    .tag("state", toggleStateText)
                    .build()

                val loreLine = messageService.builder(configService.getString(ConfigKeys.Messages.Inventories.CONFIG_TOGGLE_LORE))
                    .tag("action", actionText)
                    .build()

                val itemStack = ItemStack(if (enabled) Material.LIME_DYE else Material.GRAY_DYE)
                val meta = itemStack.itemMeta
                if (meta != null) {
                    meta.displayName(displayName)
                    meta.lore(listOf(loreLine))
                    itemStack.itemMeta = meta
                }
                return ItemWrapper(itemStack)
            }

            override fun handleClick(clickType: ClickType, clickPlayer: Player, click: Click) {
                val current = moduleLoader.isModuleEnabled(def.id)
                setModuleEnabled(def.id, !current)
                clickPlayer.playSound(clickPlayer.location, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f)
                notifyWindows()
            }
        }
    }

    private fun getFeatureItem(path: String): Item {
        return object : AbstractItem() {
            override fun getItemProvider(player: Player): ItemProvider {
                val valBoolean = configService.getBoolean(path)
                val namePrefix = configService.getString(ConfigKeys.Messages.Inventories.CONFIG_VAL_NAME)
                val lorePrefix = configService.getString(ConfigKeys.Messages.Inventories.CONFIG_VAL_LORE)

                val shortKey = path.substringAfter("modules.").substringAfter(".")

                val displayName = messageService.builder("<italic:false>$namePrefix$shortKey").build()
                val lore = messageService.builder("<italic:false>$lorePrefix$valBoolean").build()

                val itemStack = ItemStack(Material.PAPER)
                val meta = itemStack.itemMeta
                if (meta != null) {
                    meta.displayName(displayName)
                    meta.lore(listOf(lore))
                    itemStack.itemMeta = meta
                }
                return ItemWrapper(itemStack)
            }

            override fun handleClick(clickType: ClickType, clickPlayer: Player, click: Click) {
                val currentValue = configService.getBoolean(path)
                val newValue = !currentValue

                configService.set(path, newValue)
                try {
                    configService.getConfig("config.yml")?.save()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                clickPlayer.playSound(clickPlayer.location, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f)
                notifyWindows()
            }
        }
    }

    private fun setModuleEnabled(id: String, enabled: Boolean) {
        moduleLoader.toggleModule(id, enabled)
        configService.set("modules.$id", enabled)
        try {
            configService.getConfig("config.yml")?.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}