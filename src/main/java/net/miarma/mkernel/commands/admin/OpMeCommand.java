package net.miarma.mkernel.commands.admin;

import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.util.MessageUtil;
import dev.jorel.commandapi.CommandAPICommand;

public class OpMeCommand {
    public static void register() {
        CommandWrapper opMeCmd = CommandProvider.getOpMeCommand();
        new CommandAPICommand(opMeCmd.getName())
            .withFullDescription(opMeCmd.getDescription())
            .withShortDescription(opMeCmd.getDescription())
            .withPermission(opMeCmd.getPermission().base())
            .executesPlayer((sender,args) -> {
                if(!sender.isOp()) {
                    sender.setOp(true);
                    MessageUtil.sendMessage(sender, opMeCmd.getMessages()[0], true);
                } else {
                    MessageUtil.sendMessage(sender, opMeCmd.getMessages()[1], true);
                }
            })
            .register();
    }
}
