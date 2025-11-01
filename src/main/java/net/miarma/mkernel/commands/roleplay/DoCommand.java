package net.miarma.mkernel.commands.roleplay;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import dev.jorel.commandapi.CommandAPICommand;
import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.util.PlayerUtil;

public class DoCommand {
    public static void register() {
        CommandWrapper doCmd = CommandProvider.getDoCommand();
        new CommandAPICommand(doCmd.getName())
            .withArguments(CommandProvider.Arguments.message())
            .withFullDescription(doCmd.getDescription())
            .withPermission(doCmd.getPermission().base())
            .withShortDescription(doCmd.getDescription())
            .executesPlayer((sender, args) -> {
                String joinedArgs = Arrays.stream(args.rawArgs()).collect(Collectors.joining(" "));
                String msg = "§9(" + sender.getName() + ") [Do] §7" + joinedArgs;
                Bukkit.getServer().getOnlinePlayers().stream()
                        .filter(p -> (p.getWorld() == sender.getWorld()) && (PlayerUtil.distance(sender, p) < 25 || sender.equals(p)))
                        .forEach(p -> p.sendMessage(msg));
            })
            .register();
    }
}
