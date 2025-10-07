package net.miarma.mkernel.commands.admin;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import dev.jorel.commandapi.CommandAPICommand;
import net.miarma.mkernel.common.minecraft.inventories.InvseeInventory;
import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.util.MessageUtil;

public class InvseeCommand {

    public static void register() {
        CommandWrapper invseeCommand = CommandProvider.getInvseeCommand();
        new CommandAPICommand(invseeCommand.getName())
                .withArguments(CommandProvider.Arguments.playerArg())
                .withShortDescription(invseeCommand.getDescription())
                .withFullDescription(invseeCommand.getDescription())
                .withPermission(invseeCommand.getPermission().base())
                .executesPlayer((sender, args) -> {
                    Player target = (Player) args.get(0);

                    if (target == null || !target.isOnline()) {
                        MessageUtil.sendMessage(sender, "&cEse jugador no está conectado.", true);
                        return;
                    }

                    Inventory copy = Bukkit.createInventory(sender, 54, "Inventario de " + target.getName());
                    PlayerInventory tInv = target.getInventory();

                    for (int i = 0; i < 36; i++) copy.setItem(i, tInv.getItem(i));
                    copy.setItem(36, tInv.getHelmet());
                    copy.setItem(37, tInv.getChestplate());
                    copy.setItem(38, tInv.getLeggings());
                    copy.setItem(39, tInv.getBoots());
                    copy.setItem(40, tInv.getItemInOffHand());

                    InvseeInventory.setInvForAdmin(sender, copy, target); // nueva forma

                    sender.openInventory(copy);

                    MessageUtil.sendMessage(sender, invseeCommand.getMessages()[0], true,
                            List.of("%player%"), List.of(target.getName()));
                })
                .register();
    }
}
