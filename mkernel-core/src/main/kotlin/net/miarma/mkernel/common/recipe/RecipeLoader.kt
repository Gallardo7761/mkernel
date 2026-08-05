package net.miarma.mkernel.common.recipe

import com.google.inject.Inject
import com.google.inject.Singleton
import de.tr7zw.nbtapi.NBT
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.miarma.mkernel.MKernel
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

    val loadedRecipes: MutableMap<String, Recipe> = mutableMapOf()
    private val recipeHashes: MutableMap<String, Long> = mutableMapOf()
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
            saveDefaultRecipe("shop_chest.yml")
        }

        val currentFiles = folder.listFiles { _, name -> name.endsWith(".yml") } ?: return

        for (file in currentFiles) {
            val id = file.nameWithoutExtension
            val lastModified = file.lastModified()

            if (recipeHashes[id] == lastModified && loadedRecipes.containsKey(id)) {
                continue
            }

            try {
                val config = YamlConfiguration.loadConfiguration(file)
                val recipe = parseRecipe(config)

                if (recipe != null) {
                    (loadedRecipes[id] as? Keyed)?.let { Bukkit.getServer().removeRecipe(it.key) }
                    Bukkit.getServer().addRecipe(recipe)

                    loadedRecipes[id] = recipe
                    recipeHashes[id] = lastModified
                    MKernel.LOGGER.info("Custom recipe updated: $id")
                }
            } catch (e: Exception) {
                MKernel.LOGGER.severe("Error loading recipe: ${file.name}")
                e.printStackTrace()
            }
        }

        Bukkit.getServer().updateRecipes()
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

        return runCatching {
            val key = NamespacedKey(plugin, id)

            config.getConfigurationSection("properties")?.let { itemProperties[id] = it }
            val resultSection = config.getConfigurationSection("result") ?: throw IllegalArgumentException("Missing 'result' section")
            val matStr = resultSection.getString("material") ?: throw IllegalArgumentException("Missing 'material' in result")
            val mat = Material.matchMaterial(matStr) ?: throw IllegalArgumentException("Invalid material: $matStr")
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
                        val parts = enchStr.split(":")
                        if (parts.size == 2) {
                            val enchKey = parts[0].lowercase()
                            val level = parts[1].toIntOrNull() ?: 1
                            val enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)[NamespacedKey.minecraft(enchKey)]
                            enchantment?.let { meta.addEnchant(it, level, true) }
                        }
                    }
                    resultSection.getStringList("flags").forEach { flagStr ->
                        runCatching { ItemFlag.valueOf(flagStr.uppercase()) }.onSuccess { meta.addItemFlags(it) }
                    }
                }
            }

            when (config.getString("type", "SHAPED")?.uppercase()) {
                "SHAPED" -> {
                    ShapedRecipe(key, item).apply {
                        val shapeList = config.getStringList("recipe.shape")
                        if (shapeList.isNotEmpty()) {
                            shape(*shapeList.toTypedArray())
                        }

                        config.getConfigurationSection("recipe.ingredients")?.getKeys(false)?.forEach { keyChar ->
                            val ingMatStr = config.getString("recipe.ingredients.$keyChar") ?: return@forEach
                            val ingMat = Material.matchMaterial(ingMatStr) ?: throw IllegalArgumentException("Invalid ingredient material: $ingMatStr")
                            setIngredient(keyChar[0], ingMat)
                        }
                    }
                }
                "CAMPFIRE" -> {
                    val cookingSection = config.getConfigurationSection("cooking") ?: throw IllegalArgumentException("Missing 'cooking' section")
                    val ingMatStr = cookingSection.getString("ingredient") ?: throw IllegalArgumentException("Missing 'ingredient' in cooking")
                    val ingredient = Material.matchMaterial(ingMatStr) ?: throw IllegalArgumentException("Invalid cooking ingredient: $ingMatStr")

                    val experience = cookingSection.getDouble("experience", 0.0).toFloat()
                    val cookTimeTicks = cookingSection.getInt("cook_time_seconds", 10) * 20
                    org.bukkit.inventory.CampfireRecipe(key, item, ingredient, experience, cookTimeTicks).apply {
                        config.getString("category")?.let { catStr ->
                            runCatching {
                                category = CookingBookCategory.valueOf(catStr.uppercase())
                            }.onFailure {
                                MKernel.LOGGER.warning("Invalid cooking category '$catStr' in recipe $id")
                            }
                        }
                    }
                }
                else -> throw IllegalArgumentException("Unknown recipe type: ${config.getString("type")}")
            }
        }.onFailure { e ->
            MKernel.LOGGER.warning("Error parsing recipe '$id': ${e.message}")
        }.getOrNull()
    }
}
