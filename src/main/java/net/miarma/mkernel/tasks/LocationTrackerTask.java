package net.miarma.mkernel.tasks;

import net.miarma.mkernel.MKernel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class LocationTrackerTask {
    private static Map<Player, Location> LOCATIONS = new HashMap<>();

    public static void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                	if(player.getWorld().getName().equals(Bukkit.getServer().getWorlds().get(0).getName())) {
                		Location currentLocation = player.getLocation();
                		LOCATIONS.put(player, currentLocation);
                	}
                }
            }
        }.runTaskTimer(MKernel.PLUGIN, 0, 20);
    }

    public static Location getPlayerRealTimeLocation(Player player) {
        return LOCATIONS.get(player);
    }
}
