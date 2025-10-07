package net.miarma.mkernel.commands.misc;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPICommand;
import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.config.providers.MessageProvider;
import net.miarma.mkernel.util.MessageUtil;

public class SendCoordsCommand {
    public static void register() {
        CommandWrapper sendCoordsCmd = CommandProvider.getSendCoordsCommand();
        new CommandAPICommand(sendCoordsCmd.getName())
            .withArguments(CommandProvider.Arguments.playerArg())
            .withFullDescription(sendCoordsCmd.getDescription())
            .withPermission(sendCoordsCmd.getPermission().base())
            .withShortDescription(sendCoordsCmd.getDescription())
            .executesPlayer((sender, args) -> {
                if (args.count() > 1) {
                    MessageUtil.sendMessage(sender, MessageProvider.Errors.tooManyArguments(), true);
                }

                Player player = Bukkit.getPlayer(args.getRaw(0));
                Location loc = sender.getLocation();
                List<String> coords = List.of(String.valueOf(loc.getBlockX()),
                                            String.valueOf(loc.getBlockY()),
                                            String.valueOf(loc.getBlockZ()));

                MessageUtil.sendMessage(sender, sendCoordsCmd.getMessages()[0], true,
                                            List.of("%target%"), List.of(player.getName()));
                MessageUtil.sendMessage(player, sendCoordsCmd.getMessages()[1], true,
                                            List.of("%sender%", "%x%", "%y%", "%z%"),
                                            List.of(sender.getName(), coords.get(0), coords.get(1), coords.get(2)));
            })
            .register();
    }
}
