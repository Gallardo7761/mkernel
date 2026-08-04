package net.miarma.mkernel.common.integration.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import eu.decentsoftware.holograms.api.DHAPI
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.integration.IHook
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.util.*

@Singleton
class DecentHologramsHook @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) : IHook {
    override val pluginName = "DecentHolograms"
    private val serializer = LegacyComponentSerializer.legacyAmpersand()

    override fun register() {

    }

    fun createShopHologram(shopId: String, ownerUuid: UUID, location: Location, item: ItemStack, price: Double, stock: Int) {
        val holoLocation = location.clone().add(0.5, 2.5, 0.5)

        val offlineOwner = Bukkit.getOfflinePlayer(ownerUuid)

        val lines = listOf(
            "#ICON:${item.type.name}",
            serializer.serialize(
                messageService.builder(configService.getString(
                    ConfigKeys.Messages.Shops.Hologram.TITLE
                )).build()
            ),
            serializer.serialize(
                messageService.builder(configService.getString(
                    ConfigKeys.Messages.Shops.Hologram.PRICE
                ))
                    .forPlayer(offlineOwner)
                    .tag("price", price.toString())
                    .build()
            ),
            serializer.serialize(
                messageService.builder(configService.getString(
                    ConfigKeys.Messages.Shops.Hologram.STOCK
                ))
                    .tag("stock", stock.toString())
                    .build()
            )
        )

        DHAPI.createHologram(shopId, holoLocation, lines)
    }

    fun updateShopStock(shopId: String, newStock: Int) {
        val holo = DHAPI.getHologram(shopId) ?: return

        val newLine = serializer.serialize(
            messageService.builder(configService.getString(ConfigKeys.Messages.Shops.Hologram.STOCK))
                .tag("stock", newStock.toString())
                .build()
        )

        DHAPI.setHologramLine(holo, 3, newLine)
    }

    fun deleteHologram(shopId: String) {
        DHAPI.removeHologram(shopId)
    }
}