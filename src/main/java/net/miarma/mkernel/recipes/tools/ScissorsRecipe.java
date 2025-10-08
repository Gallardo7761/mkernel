package net.miarma.mkernel.recipes.tools;

import static net.miarma.mkernel.util.Constants.RECIPES;
import static net.miarma.mkernel.util.Constants.SCISSORS_KEY;
import static net.miarma.mkernel.util.Constants.SPECIAL_ITEM_TAG;

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

public class ScissorsRecipe {
	private static ItemStack crear() {
        ItemStack scissors = new ItemStack(Material.SHEARS);
        Component name = Component.text(MessageUtil.parseColors(
                MessageProvider.Items.getScissorsName()));
        List<Component> lore = List.of(
			Component.text(
				MessageUtil.parseColors(
	                MessageProvider.Items.getScissorsLore())		
			)
		);
        
        NBT.modify(scissors, nbt -> {
        	nbt.setString(SPECIAL_ITEM_TAG, SCISSORS_KEY);
        	nbt.modifyMeta((readOnlyNbt, meta) ->{
        		meta.displayName(name);
        		meta.lore(lore);
        		meta.addEnchant(Enchantment.UNBREAKING, 1, false);
        		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        	});
        });
        
        RECIPES.add(scissors);
        return scissors;
    }
    
    public static ShapedRecipe get() {
    	ItemStack scissors = crear();
        NamespacedKey scissorsRecipeKey = new NamespacedKey(MKernel.PLUGIN, SCISSORS_KEY);
        ShapedRecipe scissorsRecipe = new ShapedRecipe(scissorsRecipeKey, scissors);
        scissorsRecipe.shape(
        		" D ", 
        		"DSD", 
        		" D ");
        scissorsRecipe.setIngredient('D', Material.DIAMOND);
        scissorsRecipe.setIngredient('S', Material.SHEARS);
        return scissorsRecipe;
    }
}
