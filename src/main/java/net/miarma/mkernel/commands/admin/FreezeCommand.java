package net.miarma.mkernel.commands.admin;

import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.config.providers.MessageProvider;
import net.miarma.mkernel.util.MessageUtil;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

import static net.miarma.mkernel.util.Constants.FROZEN_KEY;

public class FreezeCommand {
    public static void register() {
        CommandWrapper freezeCmd = CommandProvider.getFreezeCommand();
        new CommandAPICommand(freezeCmd.getName())
            .withArguments(CommandProvider.Arguments.playerArg())
            .withFullDescription(freezeCmd.getDescription())
            .withShortDescription(freezeCmd.getDescription())
            .withUsage(freezeCmd.getUsage())
            .withPermission(freezeCmd.getPermission().base())
            .executesPlayer((sender, args) -> {
                Player target = Bukkit.getPlayer(args.getRaw(0));

                if (target == null) {
                    MessageUtil.sendMessage(sender, MessageProvider.Errors.playerNotFound(), true);
                }

                if(args.count() > 1) {
                    MessageUtil.sendMessage(sender, MessageProvider.Errors.tooManyArguments(), true);
                }

                PersistentDataContainer data = target.getPersistentDataContainer();
                boolean isFrozen = Boolean.TRUE.equals(data.get(FROZEN_KEY, PersistentDataType.BOOLEAN));
                data.set(FROZEN_KEY, PersistentDataType.BOOLEAN, !isFrozen);

                if(isFrozen) {
                    unsetFrozen(target);
                    MessageUtil.sendMessage(sender, freezeCmd.getMessages()[1], true,
                            List.of("%player%"), List.of(target.getName()));
                    MessageUtil.sendMessage(target, freezeCmd.getMessages()[3], true,
                            List.of("%sender%"), List.of(sender.getName()));
                } else {
                    setFrozen(target);
                    MessageUtil.sendMessage(sender, freezeCmd.getMessages()[0], true,
                            List.of("%player%"), List.of(target.getName()));
                    MessageUtil.sendMessage(target, freezeCmd.getMessages()[2], true,
                            List.of("%sender%"), List.of(sender.getName()));
                }

            })
            .register();
    }

    private static void setFrozen(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(FROZEN_KEY, PersistentDataType.BOOLEAN, true);
    }

    private static void unsetFrozen(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(FROZEN_KEY, PersistentDataType.BOOLEAN, false);
    }
}
