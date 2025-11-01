package net.miarma.mkernel.commands.roleplay;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import dev.jorel.commandapi.CommandAPICommand;
import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.util.PlayerUtil;

public class MeCommand {
    public static void register() {
        CommandWrapper meCmd = CommandProvider.getMeCommand();
        new CommandAPICommand(meCmd.getName())
            .withArguments(CommandProvider.Arguments.message())
            .withFullDescription(meCmd.getDescription())
            .withPermission(meCmd.getPermission().base())
            .withShortDescription(meCmd.getDescription())
            .executesPlayer((sender, args) -> {
                String joinedArgs = Arrays.stream(args.rawArgs()).collect(Collectors.joining(" "));
                String msg = "§6(" + sender.getName() + ") [Me] §7" + joinedArgs;
                Bukkit.getServer().getOnlinePlayers().stream()
                        .filter(p -> (p.getWorld() == sender.getWorld()) && (PlayerUtil.distance(sender, p) < 25 || sender.equals(p)))
                        .forEach(p -> p.sendMessage(msg));
            })
            .register();
    }
}
