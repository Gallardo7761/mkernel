package net.miarma.mkernel.util;

import net.miarma.mkernel.MKernel;
import net.miarma.mkernel.common.minecraft.Warp;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class PlayerUtil {
    public static double distance(Player p1, Player p2) {
        return p1.getLocation().distance(p2.getLocation());
    }

    public static boolean playersNearRadius(Player player, Collection<Player> players, int radius) {
        return players.stream().anyMatch(p -> distance(p, player) <= radius && !p.equals(player));
    }

    public static List<Warp> getWarps(Player p) {
        File f = new File(MKernel.PLUGIN.getDataFolder().getAbsolutePath(), "warps/"
                + p.getName() + ".yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        return c.getKeys(false).stream()
                .map(alias -> Warp.fromFile(c, alias))
                .toList();
    }
}
