package net.miarma.mkernel.recipes.tools;

import static net.miarma.mkernel.util.Constants.RECIPES;
import static net.miarma.mkernel.util.Constants.SPAWNER_BREAKER_KEY;
import static net.miarma.mkernel.util.Constants.SPECIAL_ITEM_TAG;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import de.tr7zw.nbtapi.NBT;
import net.kyori.adventure.text.Component;
import net.miarma.mkernel.MKernel;
import net.miarma.mkernel.config.providers.MessageProvider;
import net.miarma.mkernel.util.MessageUtil;

public class SpawnerBreakerRecipe {
	private static ItemStack crear() {
        ItemStack spawnerBreaker = new ItemStack(Material.GOLDEN_PICKAXE);
        Component name = Component.text(MessageUtil.parseColors(
                MessageProvider.Items.getSpawnerBreakerName()));
        List<Component> lore = List.of(
    		Component.text(MessageUtil.parseColors(
                MessageProvider.Items.getSpawnerBreakerLore())
			)		
		);
        
        NBT.modify(spawnerBreaker, nbt -> {
        	nbt.setString(SPECIAL_ITEM_TAG, SPAWNER_BREAKER_KEY);
        	nbt.modifyMeta((readOnlyNbt, meta) ->{
        		meta.displayName(name);
        		meta.lore(lore);
        		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        	});
        });
        
        RECIPES.add(spawnerBreaker);
        return spawnerBreaker;
    }
    
    public static ShapedRecipe get() {
    	ItemStack spawnerBreaker = crear();
        NamespacedKey spawnerBreakerRecipeKey = new NamespacedKey(MKernel.PLUGIN, SPAWNER_BREAKER_KEY);
        ShapedRecipe spawnerBreakerRecipe = new ShapedRecipe(spawnerBreakerRecipeKey, spawnerBreaker);
        spawnerBreakerRecipe.shape(
        		" N ", 
        		" P ", 
        		" D ");
        spawnerBreakerRecipe.setIngredient('N', Material.NETHERITE_INGOT);
        spawnerBreakerRecipe.setIngredient('P', Material.GOLDEN_PICKAXE);
        spawnerBreakerRecipe.setIngredient('D', Material.DIAMOND);
        return spawnerBreakerRecipe;
    }
}
