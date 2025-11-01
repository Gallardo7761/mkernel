package net.miarma.mkernel.util;

import net.miarma.mkernel.MKernel;
import net.miarma.mkernel.common.minecraft.teleport.TpaRequests;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Constants {
    public static TpaRequests TPA_REQUESTS = TpaRequests.getInstance();
    public static List<ItemStack> RECIPES = new ArrayList<>();
    public static NamespacedKey VANISH_KEY = new NamespacedKey(MKernel.PLUGIN, "vanish");
    public static NamespacedKey SPY_KEY = new NamespacedKey(MKernel.PLUGIN, "spy");
    public static NamespacedKey FROZEN_KEY = new NamespacedKey(MKernel.PLUGIN, "frozen");
    public static String SPECIAL_ITEM_TAG = "specialItem";
    public static String ZOMBIFICATION_POTION_KEY = "zombification_potion";
    public static String SPAWNER_BREAKER_KEY = "spawner_breaker";
    public static String ADMIN_STICK_KEY = "admin_stick";
    public static String SCISSORS_KEY = "scissors";
    public static String ROTTEN_CAMFIRE_KEY = "rotten_campfire";
}
