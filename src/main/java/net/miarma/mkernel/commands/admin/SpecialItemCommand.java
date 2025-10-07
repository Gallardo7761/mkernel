package net.miarma.mkernel.commands.admin;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;

import dev.jorel.commandapi.CommandAPICommand;
import net.miarma.mkernel.MKernel;
import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.config.providers.MessageProvider;
import net.miarma.mkernel.util.MessageUtil;

public class SpecialItemCommand {
    public static void register() {
        CommandWrapper specialItemCmd = CommandProvider.getSpecialItemCommand();
        new CommandAPICommand(specialItemCmd.getName())
            .withArguments(CommandProvider.Arguments.items())
            .withFullDescription(specialItemCmd.getDescription())
            .withPermission(specialItemCmd.getPermission().base())
            .withShortDescription(specialItemCmd.getDescription())
            .executesPlayer((sender, args) -> {
                String itemName = args.getRaw(0);
                Recipe specialItem = Bukkit.getServer().getRecipe(new NamespacedKey(MKernel.PLUGIN, itemName));
                if (specialItem != null) {
                    sender.getInventory().addItem(specialItem.getResult());
                    MessageUtil.sendMessage(sender, specialItemCmd.getMessages()[0], true,
                                                 List.of("%item%"), List.of(itemName));
                } else {
                    MessageUtil.sendMessage(sender, MessageProvider.Errors.itemNotFound(), true,
                                                List.of("%item%"), List.of(itemName));
                }
            })
            .register();
    }
}
