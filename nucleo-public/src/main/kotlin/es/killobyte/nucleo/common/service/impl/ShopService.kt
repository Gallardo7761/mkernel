package es.killobyte.nucleo.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.annotation.LoaderPriority
import es.killobyte.nucleo.api.common.IService
import es.killobyte.nucleo.api.model.Shop
import es.killobyte.nucleo.common.dao.ShopDao
import es.killobyte.nucleo.common.integration.impl.DecentHologramsHook
import org.bukkit.Location
import java.util.concurrent.ConcurrentHashMap

@Singleton
@LoaderPriority(LoaderPriority.LOWEST)
class ShopService @Inject constructor(
    private val plugin: NucleoPlugin,
    private val shopDao: ShopDao,
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
        val loadedShops = shopDao.getAllShops()
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
            plugin.logger.info("Loaded ${loadedShops.size} shops!")
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
            shopDao.insertShop(shop)
        }
    }

    fun deleteShop(shopId: String) {
        shops.remove(shopId)

        hookService.getHook(DecentHologramsHook::class.java).ifPresent { hook ->
            hook.deleteHologram(shopId)
        }

        plugin.launchAsync {
            shopDao.deleteShop(shopId)
        }
    }
}