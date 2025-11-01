package net.miarma.mkernel.commands.misc;

import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.config.providers.ConfigProvider;
import net.miarma.mkernel.util.FileUtil;
import net.miarma.mkernel.util.MessageUtil;
import dev.jorel.commandapi.CommandAPICommand;

import java.io.IOException;
import java.util.List;

public class InventoryRecoveryCommand {
    public static void register() {
        CommandWrapper recInvCmd = CommandProvider.getRecInvCommand();
        new CommandAPICommand(recInvCmd.getName())
            .withPermission(recInvCmd.getPermission().base())
            .withShortDescription(recInvCmd.getDescription())
            .withFullDescription(recInvCmd.getDescription())
            .executesPlayer((sender, args) -> {
                int xpLevels = sender.getLevel();
                int requiredLevels = ConfigProvider.Values.getRecInvRequiredLevel();

                if (xpLevels < requiredLevels) {
                    MessageUtil.sendMessage(sender, recInvCmd.getMessages()[1], true,
                                                 List.of("%required%"), List.of(String.valueOf(requiredLevels)));
                    return;
                }

                int items;
                try {
                    items = FileUtil.restoreInventory(sender);
                    if(items == 0) {
                        MessageUtil.sendMessage(sender, recInvCmd.getMessages()[2], true);
                        return;
                    }
                    MessageUtil.sendMessage(sender, recInvCmd.getMessages()[0], true,
                                                List.of("%items%"), List.of(String.valueOf(items)));
                    sender.setLevel(xpLevels - requiredLevels);
                    FileUtil.clearInventory(sender);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            })
            .register();
    }
}
