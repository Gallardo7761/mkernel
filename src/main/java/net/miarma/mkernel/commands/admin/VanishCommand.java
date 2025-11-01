package net.miarma.mkernel.commands.admin;

import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.util.MessageUtil;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static net.miarma.mkernel.util.Constants.VANISH_KEY;

public class VanishCommand {
    public static void register() {
        CommandWrapper vanishCmd = CommandProvider.getVanishCommand();
        new CommandAPICommand(vanishCmd.getName())
            .withAliases("v")
            .withFullDescription(vanishCmd.getDescription())
            .withShortDescription(vanishCmd.getDescription())
            .withPermission(vanishCmd.getPermission().base())
            .executesPlayer((sender,args) -> {
                PersistentDataContainer data = sender.getPersistentDataContainer();
                boolean isVanished = Boolean.TRUE.equals(data.get(VANISH_KEY, PersistentDataType.BOOLEAN));
                data.set(VANISH_KEY, PersistentDataType.BOOLEAN, !isVanished);

                if(isVanished) {
                    unsetVanish(sender);
                    MessageUtil.sendMessage(sender,
                            vanishCmd.getMessages()[1], true);
                } else {
                    setVanish(sender);
                    MessageUtil.sendMessage(sender,
                            vanishCmd.getMessages()[0], true);
                }
            })
            .register();
    }

    private static void setVanish(Player player) {
        player.setInvisible(true);
        player.setCanPickupItems(false);
    }

    private static void unsetVanish(Player player) {
        player.setInvisible(false);
        player.setCanPickupItems(true);
    }
}
