package net.miarma.mkernel.commands.troll;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPICommand;
import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.util.MessageUtil;

public class LaunchCommand {
    public static void register() {
        CommandWrapper launchCmd = CommandProvider.getLaunchCommand();
        new CommandAPICommand(launchCmd.getName())
            .withArguments(CommandProvider.Arguments.playerArg())
            .withPermission(launchCmd.getPermission().base())
            .withFullDescription(launchCmd.getDescription())
            .withShortDescription(launchCmd.getDescription())
            .withUsage(launchCmd.getUsage())
            .executesPlayer((sender, args) -> {
                Player target = Bukkit.getPlayer(args.getRaw(0));
                target.setVelocity(target.getVelocity().setY(3));
                MessageUtil.sendMessage(sender, launchCmd.getMessages()[0], true,
                        List.of("%player%"), List.of(target.getName()));
            })
            .register();
    }
}
