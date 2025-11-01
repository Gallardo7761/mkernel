package net.miarma.mkernel.commands.admin;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;

import dev.jorel.commandapi.CommandAPICommand;
import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.util.MessageUtil;

public class GmcCommand {
    public static void register() {
        CommandWrapper gmcCmd = CommandProvider.getGmcCommand();
        new CommandAPICommand(gmcCmd.getName())
            .withOptionalArguments(
            		CommandProvider.Arguments.playersOptArg().withPermission(
                    gmcCmd.getPermission().others()
                )
            )
            .withShortDescription(gmcCmd.getDescription())
            .withFullDescription(gmcCmd.getDescription())
            .withUsage(gmcCmd.getUsage())
            .executesPlayer((sender,args) -> {
                if(args.rawArgs().length == 0) {
                    sender.setGameMode(GameMode.CREATIVE);
                    MessageUtil.sendMessage(sender, gmcCmd.getMessages()[0], true);
                } else {
                    Bukkit.getPlayer(args.getRaw(0)).setGameMode(GameMode.CREATIVE);
                    MessageUtil.sendMessage(sender, gmcCmd.getMessages()[1], true,
                                                List.of("%player%"), List.of(args.getRaw(0)));
                }
            })
            .register();
    }
}
