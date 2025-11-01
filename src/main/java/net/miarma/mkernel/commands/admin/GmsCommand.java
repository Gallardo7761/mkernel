package net.miarma.mkernel.commands.admin;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;

import dev.jorel.commandapi.CommandAPICommand;
import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.util.MessageUtil;

public class GmsCommand {
    public static void register() {
        CommandWrapper gmsCmd = CommandProvider.getGmsCommand();
        new CommandAPICommand(gmsCmd.getName())
            .withOptionalArguments(
            		CommandProvider.Arguments.playersOptArg().withPermission(
                    gmsCmd.getPermission().others()
                )
            )
            .withShortDescription(gmsCmd.getDescription())
            .withFullDescription(gmsCmd.getDescription())
            .withUsage(gmsCmd.getUsage())
            .executesPlayer((sender,args) -> {
                if(args.rawArgs().length == 0) {
                    sender.setGameMode(GameMode.SURVIVAL);
                    MessageUtil.sendMessage(sender, gmsCmd.getMessages()[0], true);
                } else {
                    Bukkit.getPlayer(args.getRaw(0)).setGameMode(GameMode.SURVIVAL);
                    MessageUtil.sendMessage(sender, gmsCmd.getMessages()[1], true,
                                                List.of("%player%"), List.of(args.getRaw(0)));
                }
            })
            .register();
    }
}
