package net.miarma.mkernel.common.recipe

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import de.tr7zw.nbtapi.NBT
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.miarma.mkernel.common.service.impl.MessageService
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.recipe.CookingBookCategory
import java.io.File

@Singleton
class RecipeLoader @Inject constructor(
    private val plugin: MKernel,
    private val messageService: MessageService
) {

    val loadedRecipes: MutableList<Recipe> = mutableListOf()
    private val itemProperties = mutableMapOf<String, ConfigurationSection>()

    companion object {
        private const val SPECIAL_ITEM_TAG = "special_item_tag"
    }

    fun loadAll() {
        val folder = File(plugin.dataFolder, "recipes").apply { mkdirs() }

        if (folder.listFiles().isNullOrEmpty()) {
            MKernel.LOGGER.info("Recipes folder is empty. Copying default recipes...")
            saveDefaultRecipe("admin_stick.yml")
            saveDefaultRecipe("scissors.yml")
            saveDefaultRecipe("spawner_breaker.yml")
            saveDefaultRecipe("zombification_potion.yml")
            saveDefaultRecipe("rotten_campfire.yml")
        }

        folder.listFiles { _, name -> name.endsWith(".yml") }?.forEach { file ->
            try {
                val config = YamlConfiguration.loadConfiguration(file)
                parseRecipe(config)?.let {
                    Bukkit.getServer().addRecipe(it)
                    loadedRecipes.add(it)
                    MKernel.LOGGER.info("Custom recipe loaded: ${config.getString("id")}")
                }
            } catch (e: Exception) {
                MKernel.LOGGER.severe("Error loading recipe: ${file.name}")
                e.printStackTrace()
            }
        }
    }

    fun unloadAll() {
        MKernel.LOGGER.info("Unloading ${loadedRecipes.size} custom recipes...")
        loadedRecipes.forEach { recipe ->
            (recipe as? Keyed)?.let { Bukkit.getServer().removeRecipe(it.key) }
        }
        loadedRecipes.clear()
        itemProperties.clear()
    }

    private fun saveDefaultRecipe(fileName: String) {
        val recipeFile = File(plugin.dataFolder, "recipes/$fileName")
        if (!recipeFile.exists()) {
            plugin.saveResource("recipes/$fileName", false)
        }
    }

    fun getProperties(itemId: String): ConfigurationSection? = itemProperties[itemId]

    private fun parseRecipe(config: YamlConfiguration): Recipe? {
        val id = config.getString("id") ?: return null
        val key = NamespacedKey(plugin, id)

        config.getConfigurationSection("properties")?.let { itemProperties[id] = it }
        val resultSection = config.getConfigurationSection("result") ?: return null
        val mat = Material.valueOf(resultSection.getString("material")!!)
        val item = ItemStack(mat)

        NBT.modify(item) { nbt ->
            nbt.setString(SPECIAL_ITEM_TAG, id)
            resultSection.getConfigurationSection("nbt")?.getKeys(false)?.forEach { nbtKey ->
                nbt.setString(nbtKey, resultSection.getString("nbt.$nbtKey"))
            }

            nbt.modifyMeta { _, meta ->
                resultSection.getString("name")?.let { meta.displayName(messageService.builder(it).build()) }
                resultSection.getStringList("lore").takeIf { it.isNotEmpty() }?.let {
                    meta.lore(it.map { line -> messageService.builder(line).build() })
                }
                resultSection.getStringList("enchants").forEach { enchStr ->
                    val (ench, level) = enchStr.split(":")
                    val enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)[NamespacedKey.minecraft(ench.lowercase())]
                    enchantment?.let { meta.addEnchant(it, level.toInt(), true) }
                }
                resultSection.getStringList("flags").forEach { flagStr ->
                    meta.addItemFlags(ItemFlag.valueOf(flagStr))
                }
            }
        }

        return when (config.getString("type", "SHAPED")?.uppercase()) {
            "SHAPED" -> {
                ShapedRecipe(key, item).apply {
                    shape(*config.getStringList("recipe.shape").toTypedArray())
                    config.getConfigurationSection("recipe.ingredients")?.getKeys(false)?.forEach { keyChar ->
                        setIngredient(keyChar[0], Material.valueOf(config.getString("recipe.ingredients.$keyChar")!!))
                    }
                }
            }
            "CAMPFIRE" -> {
                val cookingSection = config.getConfigurationSection("cooking") ?: return null
                val ingredient = Material.valueOf(cookingSection.getString("ingredient")!!)
                val experience = cookingSection.getDouble("experience", 0.0).toFloat()
                val cookTimeTicks = cookingSection.getInt("cook_time_seconds", 10) * 20
                org.bukkit.inventory.CampfireRecipe(key, item, ingredient, experience, cookTimeTicks).apply {
                    config.getString("category")?.let {
                        try {
                            category = CookingBookCategory.valueOf(it.uppercase())
                        } catch (e: IllegalArgumentException) {
                            MKernel.LOGGER.warning("Invalid cooking category in $id")
                        }
                    }
                }
            }
            else -> null
        }
    }
}
