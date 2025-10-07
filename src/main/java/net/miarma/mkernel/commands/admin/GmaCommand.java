package net.miarma.mkernel.commands.admin;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;

import dev.jorel.commandapi.CommandAPICommand;
import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.util.MessageUtil;

public class GmaCommand {
    public static void register() {
        CommandWrapper gmaCmd = CommandProvider.getGmaCommand();
        new CommandAPICommand(gmaCmd.getName())
            .withOptionalArguments(
            		CommandProvider.Arguments.playersOptArg().withPermission(
                    gmaCmd.getPermission().others()
                )
            )
            .withShortDescription(gmaCmd.getDescription())
            .withFullDescription(gmaCmd.getDescription())
            .withUsage(gmaCmd.getUsage())
            .executesPlayer((sender,args) -> {
                if(args.rawArgs().length == 0) {
                    sender.setGameMode(GameMode.ADVENTURE);
                    MessageUtil.sendMessage(sender, gmaCmd.getMessages()[0], true);
                } else {
                    Bukkit.getPlayer(args.getRaw(0)).setGameMode(GameMode.ADVENTURE);
                    MessageUtil.sendMessage(sender, gmaCmd.getMessages()[1], true,
                                                List.of("%player%"), List.of(args.getRaw(0)));
                }
            })
            .register();
    }
}
