package net.miarma.mkernel.util;

import net.miarma.mkernel.MKernel;
import net.miarma.mkernel.config.providers.MessageProvider;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class FileUtil {
    public static void copyResourceToFile(String resourceName, String destinationPath) throws IOException {
        ClassLoader classLoader = MessageUtil.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(resourceName);
             FileOutputStream outputStream = new FileOutputStream(destinationPath)) {

            byte[] buffer = new byte[1024];
            int length;
            while (true) {
                assert inputStream != null;
                if ((length = inputStream.read(buffer)) == -1) break;
                outputStream.write(buffer, 0, length);
            }
        }
    }

    public static void createLangs(String fileName) {
        try {
            File langs = new File(MKernel.PLUGIN.getDataFolder(), fileName);
            langs.createNewFile();
            copyResourceToFile(fileName, new File(MKernel.PLUGIN.getDataFolder(), fileName).getAbsolutePath());
        } catch (IOException e) {
            MKernel.PLUGIN.getLogger().severe(MessageProvider.Errors.failedCreatingLangs());
        }
    }

    public static void saveInventory(Player p) throws IOException{
        File f = new File(MKernel.PLUGIN.getDataFolder().getAbsolutePath(), "inventories/"
                + p.getName() + ".yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        c.set("inventory", p.getInventory().getContents());
        c.save(f);
    }

    @SuppressWarnings("unchecked")
    public static int restoreInventory(Player p) throws IOException {
        File f = new File(MKernel.PLUGIN.getDataFolder().getAbsolutePath(), "inventories/"
                + p.getName() + ".yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        ItemStack[] contents;
        if(c.get("inventory") != null) {
            contents = ((List<ItemStack>) c.get("inventory")).toArray(new ItemStack[0]);
        } else {
            return 0;
        }

        Arrays.stream(contents)
                .filter(item -> item != null && item.getType() != Material.AIR)
                .forEach(item -> p.getInventory().addItem(item));
        return Arrays.stream(contents)
                .filter(item -> item != null && item.getType() != Material.AIR)
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    public static void clearInventory(Player p) throws IOException {
        File f = new File(MKernel.PLUGIN.getDataFolder().getAbsolutePath(), "inventories/"
                + p.getName() + ".yml");
        Files.delete(f.toPath());
    }
}
