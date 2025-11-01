package net.miarma.mkernel.recipes.misc;

import static net.miarma.mkernel.util.Constants.*;

import net.miarma.mkernel.MKernel;
import net.miarma.mkernel.config.providers.ConfigProvider;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.recipe.CookingBookCategory;

public class RottenFleshCampfireRecipe {
    public static CampfireRecipe get() {
        NamespacedKey rottenRecipeKey = new NamespacedKey(MKernel.PLUGIN, ROTTEN_CAMFIRE_KEY);
        CampfireRecipe rottenRecipe = new CampfireRecipe(
        		rottenRecipeKey, 
        		new ItemStack(Material.BEEF), 
        		Material.ROTTEN_FLESH, 
        		0, ConfigProvider.Values.getRottenFleshCookTime() * 20);
        rottenRecipe.setCategory(CookingBookCategory.FOOD);
        return rottenRecipe;
    }

}
