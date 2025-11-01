package net.miarma.mkernel.commands.admin;

import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.util.MessageUtil;
import dev.jorel.commandapi.CommandAPICommand;

public class ThunderCommand {
    public static void register() {
        CommandWrapper thunderCmd = CommandProvider.getThunderCommand();
        new CommandAPICommand(thunderCmd.getName())
            .withShortDescription(thunderCmd.getDescription())
            .withFullDescription(thunderCmd.getDescription())
            .withPermission(thunderCmd.getPermission().base())
            .executesPlayer((sender,args) -> {
                sender.getWorld().setStorm(true);
                sender.getWorld().setThundering(true);
                MessageUtil.sendMessage(sender, thunderCmd.getMessages()[0], true);
            })
            .register();
    }
}
