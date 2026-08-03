package net.miarma.mkernel.common.inventory

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.config.ConfigKeys
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
    private val messageService: MessageService
) {

    fun open(player: Player) {
        val confSec = configService.getSection("modules") ?: return
        val values = confSec.getStringRouteMappedValues(false)

        val namePrefix = configService.getString(ConfigKeys.Messages.Inventories.CONFIG_VAL_NAME)
        val lorePrefix = configService.getString(ConfigKeys.Messages.Inventories.CONFIG_VAL_LORE)

        val configItems = mutableListOf<Item>()

        for ((key, value) in values) {
            if (value !is Boolean) continue
            configItems.add(getItem(key, namePrefix, lorePrefix))
        }

        val totalItems = configItems.size
        val rows = max(1, ceil(totalItems / 9.0).toInt())

        val structure = Array(rows) { "x x x x x x x x x" }

        val gui = PagedGui.itemsBuilder()
            .setStructure(*structure)
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .setContent(configItems)
            .build()

        val title = messageService.builder(configService.getString(ConfigKeys.Messages.Inventories.CONFIG_TITLE)).build()

        Window.builder()
            .setTitle(title)
            .setUpperGui(gui)
            .setViewer(player)
            .build()
            .open()
    }

    private fun getItem(key: String, namePrefix: String, lorePrefix: String): Item {
        return object : AbstractItem() {
            override fun getItemProvider(player: Player): ItemProvider {
                val valBoolean = configService.getBoolean("modules.$key")
                val displayName = messageService.builder("<italic:false>$namePrefix$key").build()
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
                val configKey = "modules.$key"
                val currentValue = configService.getBoolean(configKey)
                val newValue = !currentValue

                configService.set(configKey, newValue)
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
}