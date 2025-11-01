package net.miarma.mkernel.commands.admin;

import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.util.MessageUtil;
import dev.jorel.commandapi.CommandAPICommand;

public class SunCommand {
    public static void register() {
        CommandWrapper sunCmd = CommandProvider.getSunCommand();
        new CommandAPICommand(sunCmd.getName())
            .withShortDescription(sunCmd.getDescription())
            .withFullDescription(sunCmd.getDescription())
            .withPermission(sunCmd.getPermission().base())
            .executesPlayer((sender,args) -> {
                sender.getWorld().setStorm(false);
                sender.getWorld().setThundering(false);
                MessageUtil.sendMessage(sender, sunCmd.getMessages()[0], true);
            })
            .register();
    }
}
