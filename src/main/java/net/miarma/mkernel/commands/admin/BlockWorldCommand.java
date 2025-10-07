package net.miarma.mkernel.commands.admin;

import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.config.providers.MessageProvider;
import net.miarma.mkernel.tasks.LocationTrackerTask;
import net.miarma.mkernel.util.MessageUtil;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static net.miarma.mkernel.MKernel.WORLD_BLOCKER_CONFIG;

import java.util.List;

public class BlockWorldCommand {
    public static void register() {
        CommandWrapper blockWorldCmd = CommandProvider.getBlockWorldCommand();
        List<String> blockedWorlds = WORLD_BLOCKER_CONFIG.getConfig().getStringList("blockedWorlds");
        new CommandAPICommand(blockWorldCmd.getName())
            .withArguments(CommandProvider.Arguments.worlds())
            .withAliases(blockWorldCmd.getAliases())
            .withFullDescription(blockWorldCmd.getDescription())
            .withPermission(blockWorldCmd.getPermission().base())
            .withShortDescription(blockWorldCmd.getDescription())
            .executesPlayer((sender, args) -> {
                if (args.count() != 1) {
                    MessageUtil.sendMessage(sender, MessageProvider.Errors.invalidArgument(), true);
                }

                String world = args.getRaw(0);

                if (blockedWorlds.contains(world)) {
                    blockedWorlds.remove(world);
                    MessageUtil.sendMessage(sender, blockWorldCmd.getMessages()[1], true,
                                                List.of("%world%"), List.of(world));
                } else {
                    blockedWorlds.add(world);
                    List<Player> playersInWorld = Bukkit.getWorld(world).getPlayers();
                    if(!playersInWorld.isEmpty()) {
                        playersInWorld.forEach(p -> p.teleport(LocationTrackerTask.getPlayerRealTimeLocation(p)));
                    }
                    MessageUtil.sendMessage(sender, blockWorldCmd.getMessages()[0], true,
                                                List.of("%world%"), List.of(world));
                }

                WORLD_BLOCKER_CONFIG.getConfig().set("blockedWorlds", blockedWorlds);
                WORLD_BLOCKER_CONFIG.saveConfig();
            })
            .register();
    }
}
