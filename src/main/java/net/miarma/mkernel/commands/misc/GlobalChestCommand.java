package net.miarma.mkernel.commands.misc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPICommand;
import net.miarma.mkernel.common.minecraft.inventories.GlobalChest;
import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.config.providers.MessageProvider;
import net.miarma.mkernel.util.MessageUtil;

public class GlobalChestCommand {
    public static void register() {
        CommandWrapper globalChestCmd = CommandProvider.getGlobalChestCommand();
        new CommandAPICommand(globalChestCmd.getName())
            .withOptionalArguments(CommandProvider.Arguments.playersOptArg().withPermission(
                    globalChestCmd.getPermission().others()
            ))
            .withFullDescription(globalChestCmd.getDescription())
            .withAliases(globalChestCmd.getAliases())
            .withPermission(globalChestCmd.getPermission().base())
            .withShortDescription(globalChestCmd.getDescription())
            .executesPlayer((sender, args) -> {
                if (args.count() > 1) {
                    MessageUtil.sendMessage(sender, MessageProvider.Errors.tooManyArguments(), true);
                } else if (args.count() == 0) {
                    Player player = sender;
                    player.openInventory(GlobalChest.getInv());
                } else if (args.count() == 1) {
                    Player player = Bukkit.getServer().getPlayer(args.getRaw(0));
                    player.openInventory(GlobalChest.getInv());
                }
            })
            .register();
    }
}
