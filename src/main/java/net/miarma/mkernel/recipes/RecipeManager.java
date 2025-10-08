package net.miarma.mkernel.recipes;

import static net.miarma.mkernel.util.Constants.ADMIN_STICK_KEY;
import static net.miarma.mkernel.util.Constants.ROTTEN_CAMFIRE_KEY;
import static net.miarma.mkernel.util.Constants.SCISSORS_KEY;
import static net.miarma.mkernel.util.Constants.SPAWNER_BREAKER_KEY;
import static net.miarma.mkernel.util.Constants.ZOMBIFICATION_POTION_KEY;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

import net.miarma.mkernel.MKernel;
import net.miarma.mkernel.recipes.misc.RottenFleshCampfireRecipe;
import net.miarma.mkernel.recipes.potions.ZombificationPotionRecipe;
import net.miarma.mkernel.recipes.tools.AdminStickRecipe;
import net.miarma.mkernel.recipes.tools.ScissorsRecipe;
import net.miarma.mkernel.recipes.tools.SpawnerBreakerRecipe;

public class RecipeManager {
	public static void onEnable() {
		Bukkit.getServer().addRecipe(AdminStickRecipe.get());
		Bukkit.getServer().addRecipe(ScissorsRecipe.get());
		Bukkit.getServer().addRecipe(RottenFleshCampfireRecipe.get());
		Bukkit.getServer().addRecipe(SpawnerBreakerRecipe.get());
		Bukkit.getServer().addRecipe(ZombificationPotionRecipe.get());
	}
	
	public static void onDisable() {
		Bukkit.getServer().removeRecipe(new NamespacedKey(MKernel.PLUGIN, ADMIN_STICK_KEY));
		Bukkit.getServer().removeRecipe(new NamespacedKey(MKernel.PLUGIN, SCISSORS_KEY));
		Bukkit.getServer().removeRecipe(new NamespacedKey(MKernel.PLUGIN, ROTTEN_CAMFIRE_KEY));
		Bukkit.getServer().removeRecipe(new NamespacedKey(MKernel.PLUGIN, SPAWNER_BREAKER_KEY));
		Bukkit.getServer().removeRecipe(new NamespacedKey(MKernel.PLUGIN, ZOMBIFICATION_POTION_KEY));
	}
}
