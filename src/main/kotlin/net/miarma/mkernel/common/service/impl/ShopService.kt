package net.miarma.mkernel.common.service.impl

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.annotation.LoaderPriority
import net.miarma.mkernel.common.integration.impl.DecentHologramsHook
import net.miarma.mkernel.common.model.Shop
import net.miarma.mkernel.common.service.IService
import org.bukkit.Location
import java.util.concurrent.ConcurrentHashMap

@Singleton
@LoaderPriority(LoaderPriority.LOWEST)
class ShopService @Inject constructor(
    private val plugin: MKernel,
    private val databaseService: DatabaseService,
    private val hookService: HookService
) : IService {

    private val shops = ConcurrentHashMap<String, Shop>()

    override fun onEnable() {
        plugin.launchAsync {
            loadShops()
        }
    }

    override fun onDisable() {
        val dhHook = hookService.getHook(DecentHologramsHook::class.java).orElse(null) ?: return
        shops.keys.forEach { dhHook.deleteHologram(it) }
    }

    suspend fun loadShops() {
        shops.clear()
        val loadedShops = databaseService.getAllShops()
        plugin.launchSync {
            for (shop in loadedShops) {
                shops[shop.id] = shop

                hookService.getHook(DecentHologramsHook::class.java).ifPresent { hook ->
                    hook.createShopHologram(
                        shopId = shop.id,
                        ownerUuid = shop.ownerUuid,
                        location = shop.location,
                        item = shop.item,
                        price = shop.price,
                        stock = shop.stock
                    )
                }
            }
            MKernel.LOGGER.info("Loaded ${loadedShops.size} shops!")
        }
    }

    fun generateShopId(location: Location): String {
        return "${location.world.name}_${location.blockX}_${location.blockY}_${location.blockZ}"
    }

    fun getShopAt(location: Location): Shop? {
        return shops[generateShopId(location)]
    }

    fun syncShop(shop: Shop) {
        shops[shop.id] = shop

        hookService.getHook(DecentHologramsHook::class.java).ifPresent { hook ->
            hook.deleteHologram(shop.id)
            hook.createShopHologram(shop.id, shop.ownerUuid, shop.location, shop.item, shop.price, shop.stock)
        }

        plugin.launchAsync {
            databaseService.insertShop(shop)
        }
    }

    fun deleteShop(shopId: String) {
        shops.remove(shopId)

        hookService.getHook(DecentHologramsHook::class.java).ifPresent { hook ->
            hook.deleteHologram(shopId)
        }

        plugin.launchAsync {
            databaseService.deleteShop(shopId)
        }
    }
}