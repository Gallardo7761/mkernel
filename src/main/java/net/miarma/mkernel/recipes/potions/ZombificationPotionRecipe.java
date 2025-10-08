package net.miarma.mkernel.recipes.potions;

import static net.miarma.mkernel.util.Constants.RECIPES;
import static net.miarma.mkernel.util.Constants.SPECIAL_ITEM_TAG;
import static net.miarma.mkernel.util.Constants.ZOMBIFICATION_POTION_KEY;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import de.tr7zw.nbtapi.NBT;
import net.kyori.adventure.text.Component;
import net.miarma.mkernel.MKernel;
import net.miarma.mkernel.config.providers.MessageProvider;
import net.miarma.mkernel.util.MessageUtil;

public class ZombificationPotionRecipe {
	private static ItemStack crear() {
		ItemStack potion = new ItemStack(Material.SPLASH_POTION);
		Component name = Component.text(MessageUtil.parseColors(
                MessageProvider.Items.getZombificationPotionName()));
        List<Component> lore = List.of(
    		Component.text(MessageUtil.parseColors(
                MessageProvider.Items.getZombificationPotionLore())
			)		
		);

        NBT.modify(potion, nbt -> {
        	nbt.setString(SPECIAL_ITEM_TAG, ZOMBIFICATION_POTION_KEY);
        	nbt.modifyMeta((readOnlyNbt, meta) ->{
        		meta.displayName(name);
        		meta.lore(lore);
        		meta.addEnchant(Enchantment.MENDING, 1, false);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        	});
        });
        
        RECIPES.add(potion);
        return potion;
    }
    
    public static ShapedRecipe get() {
    	ItemStack potion = crear();
    	
        NamespacedKey zombificationKey = new NamespacedKey(MKernel.PLUGIN, ZOMBIFICATION_POTION_KEY);
        ShapedRecipe recipe = new ShapedRecipe(zombificationKey, potion);
        recipe.shape(
        		" Z ", 
        		" P ", 
        		"   ");
        recipe.setIngredient('Z', Material.ZOMBIE_HEAD);
        recipe.setIngredient('P', Material.SPLASH_POTION);
        return recipe;
    }
}
