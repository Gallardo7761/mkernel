package net.miarma.mkernel.commands.misc;

import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.util.MessageUtil;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import static net.miarma.mkernel.MKernel.HOME_CONFIG;

public class HomeCommand {
    public static void register() {
        CommandWrapper homeCmd = CommandProvider.getHomeCommand();
        new CommandAPICommand(homeCmd.getName())
            .withPermission(homeCmd.getPermission().base())
            .withFullDescription(homeCmd.getDescription())
            .withShortDescription(homeCmd.getDescription())
            .executesPlayer((sender, args) -> {
                String path = "homes." + sender.getName();
                if(HOME_CONFIG.getConfig().contains(path)) {
                    World world = Bukkit.getWorld(HOME_CONFIG.getConfig().getString(path + ".world"));
                    double x = HOME_CONFIG.getConfig().getDouble(path + ".x");
                    double y = HOME_CONFIG.getConfig().getDouble(path + ".y");
                    double z = HOME_CONFIG.getConfig().getDouble(path + ".z");
                    float yaw = (float) HOME_CONFIG.getConfig().getDouble(path + ".yaw");
                    float pitch = (float) HOME_CONFIG.getConfig().getDouble(path + ".pitch");
                    Location loc = new Location(world, x, y, z, yaw, pitch);
                    sender.teleport(loc);
                    MessageUtil.sendMessage(sender, homeCmd.getMessages()[0], true);
                } else {
                    MessageUtil.sendMessage(sender, homeCmd.getMessages()[1], true);
                }
            })
            .register();
    }
}
