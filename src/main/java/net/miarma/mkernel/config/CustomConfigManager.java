package net.miarma.mkernel.config;

import net.miarma.mkernel.MKernel;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class CustomConfigManager {

    private JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;

    public CustomConfigManager(JavaPlugin plugin, String fileName) {
    	this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), fileName);
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            MKernel.LOGGER.severe(e.getMessage());
        }
    }
    
    public void loadConfig() {
    	try {
    		YamlConfiguration.loadConfiguration(configFile);
    	} catch(Exception e) {
    		MKernel.LOGGER.severe(e.getMessage());
    	}
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            plugin.saveResource(configFile.getName(), false);
        }
    }

    // Métodos adicionales para obtener datos específicos de la configuración si es necesario

    public String getString(String path) {
        return config.getString(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    // Otros métodos según tus necesidades

}
