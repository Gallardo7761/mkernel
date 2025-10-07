package net.miarma.mkernel.commands.misc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPICommand;
import net.miarma.mkernel.common.minecraft.inventories.DisposalInventory;
import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.config.providers.MessageProvider;
import net.miarma.mkernel.util.MessageUtil;

public class DisposalCommand {
    public static void register() {
        CommandWrapper disposalCmd = CommandProvider.getDisposalCommand();
        new CommandAPICommand(disposalCmd.getName())
            .withOptionalArguments(CommandProvider.Arguments.playersOptArg().withPermission(
                disposalCmd.getPermission().others()
            ))
            .withFullDescription(disposalCmd.getDescription())
            .withPermission(disposalCmd.getPermission().base())
            .withShortDescription(disposalCmd.getDescription())
            .executesPlayer((sender,args) -> {
                if (args.count() > 1) {
                    MessageUtil.sendMessage(sender, MessageProvider.Errors.tooManyArguments(), true);
                } else if (args.count() == 0) {
                    Player player = sender;
                    player.openInventory(DisposalInventory.getInv());
                } else if (args.count() == 1) {
                    Player player = Bukkit.getServer().getPlayer(args.getRaw(0));
                    player.openInventory(DisposalInventory.getInv());
                }
            })
            .register();
    }
}
