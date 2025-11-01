package net.miarma.mkernel.commands.admin;

import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.util.MessageUtil;
import dev.jorel.commandapi.CommandAPICommand;

public class RainCommand {
    public static void register() {
        CommandWrapper rainCmd = CommandProvider.getRainCommand();
        new CommandAPICommand(rainCmd.getName())
            .withShortDescription(rainCmd.getDescription())
            .withFullDescription(rainCmd.getDescription())
            .withPermission(rainCmd.getPermission().base())
            .executesPlayer((sender,args) -> {
                sender.getWorld().setStorm(true);
                sender.getWorld().setThundering(false);
                MessageUtil.sendMessage(sender, rainCmd.getMessages()[0], true);
            })
            .register();
    }
}
