package net.miarma.mkernel.recipes.tools;

import static net.miarma.mkernel.util.Constants.ADMIN_STICK_KEY;
import static net.miarma.mkernel.util.Constants.RECIPES;
import static net.miarma.mkernel.util.Constants.SPECIAL_ITEM_TAG;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import de.tr7zw.nbtapi.NBT;
import net.kyori.adventure.text.Component;
import net.miarma.mkernel.MKernel;
import net.miarma.mkernel.config.providers.MessageProvider;
import net.miarma.mkernel.util.MessageUtil;

public class AdminStickRecipe {
	public static ItemStack crear() {
        ItemStack stick = new ItemStack(Material.STICK);
        Component name = Component.text(MessageUtil.parseColors(
                MessageProvider.Items.getAdminStickName()));
        List<Component> lore = List.of(
    		Component.text(MessageUtil.parseColors(
                MessageProvider.Items.getAdminStickLore())
			)		
		);
                
        NBT.modify(stick, nbt -> {
        	nbt.setString(SPECIAL_ITEM_TAG, ADMIN_STICK_KEY);
        	nbt.modifyMeta((readOnlyNbt, meta) ->{
        		meta.displayName(name);
        		meta.lore(lore);
        	});
        });
        
        RECIPES.add(stick);
        return stick;
    }
    
    public static ShapedRecipe get() {
    	ItemStack palo = crear();
        NamespacedKey paloRecipeKey = new NamespacedKey(MKernel.PLUGIN, ADMIN_STICK_KEY);
        ShapedRecipe paloRecipe = new ShapedRecipe(paloRecipeKey, palo);
        paloRecipe.shape(
        		"DDD", 
        		"DSD", 
        		"DDD");
        paloRecipe.setIngredient('D', Material.BEDROCK);
        paloRecipe.setIngredient('S', Material.STICK);
        return paloRecipe;
    }
    
    
}

