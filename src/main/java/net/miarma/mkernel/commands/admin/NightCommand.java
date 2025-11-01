package net.miarma.mkernel.commands.admin;

import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.util.MessageUtil;
import dev.jorel.commandapi.CommandAPICommand;

public class NightCommand {
    public static void register() {
        CommandWrapper nightCmd = CommandProvider.getNightCommand();
        new CommandAPICommand(nightCmd.getName())
            .withShortDescription(nightCmd.getDescription())
            .withFullDescription(nightCmd.getDescription())
            .withPermission(nightCmd.getPermission().base())
            .executesPlayer((sender,args) -> {
                sender.getWorld().setTime(13000);
                MessageUtil.sendMessage(sender, nightCmd.getMessages()[0], true);
            })
            .register();
    }
}
