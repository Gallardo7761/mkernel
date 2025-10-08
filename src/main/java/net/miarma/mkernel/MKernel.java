package net.miarma.mkernel;

import java.io.File;
import java.nio.file.Files;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import net.miarma.mkernel.commands.CommandHandler;
import net.miarma.mkernel.common.minecraft.inventories.GlobalChest;
import net.miarma.mkernel.config.ConfigWrapper;
import net.miarma.mkernel.config.CustomConfigManager;
import net.miarma.mkernel.events.EventListener;
import net.miarma.mkernel.recipes.RecipeManager;
import net.miarma.mkernel.tasks.LocationTrackerTask;
import net.miarma.mkernel.util.FileUtil;

public class MKernel extends JavaPlugin {

    public static MKernel PLUGIN;
    public static final ConfigWrapper CONFIG = new ConfigWrapper();
    public static CustomConfigManager HOME_CONFIG;
    public static CustomConfigManager WORLD_BLOCKER_CONFIG;
    public static Logger LOGGER;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(
    		new CommandAPIPaperConfig(this)
        		.verboseOutput(true)
        		.setNamespace("mkernel")
		);
    }
    
    @Override    
    public void onEnable() {
        // main plugin stuff
    	super.onEnable();
        PLUGIN = this;
        LOGGER = PLUGIN.getLogger();
        CONFIG.onEnable();
                
        // files handling
        HOME_CONFIG = new CustomConfigManager(PLUGIN, "homes.yml");
        WORLD_BLOCKER_CONFIG = new CustomConfigManager(PLUGIN, "blockedWorlds.yml");

        if(!Files.exists(PLUGIN.getDataFolder().toPath().resolve("inventories/"))) {
            File file = new File(PLUGIN.getDataFolder(), "inventories/");
            file.mkdirs();
        }

        if(!Files.exists(PLUGIN.getDataFolder().toPath().resolve("warps/"))) {
            File file = new File(PLUGIN.getDataFolder(), "warps/");
            file.mkdirs();
        }

        FileUtil.createLangs("lang.yml");
        
        // onEnable methods
        CommandAPI.onEnable();
        CommandHandler.onEnable();
        RecipeManager.onEnable();
        EventListener.onEnable();
        
        // final stage: location tracker and GC
        LocationTrackerTask.start();
        
        GlobalChest.loadConfig();
        GlobalChest.loadChest();
        
        PLUGIN.getLogger().info("I've been enabled! :)");
    }

    @Override
    public void onDisable() {
        GlobalChest.saveChest();
        CommandAPI.onDisable();
        PLUGIN.getLogger().info("I've been disabled! :(");
    }
}
