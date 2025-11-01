package net.miarma.mkernel.commands.tp;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPICommand;
import net.miarma.mkernel.MKernel;
import net.miarma.mkernel.common.minecraft.teleport.TpaRequests;
import net.miarma.mkernel.common.minecraft.teleport.TpaType;
import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.config.providers.MessageProvider;
import net.miarma.mkernel.util.MessageUtil;

public class TpaHereCommand {
    public static void register() {
        CommandWrapper tpaHereCmd = CommandProvider.getTpaHereCommand();
        new CommandAPICommand(tpaHereCmd.getName())
                .withArguments(CommandProvider.Arguments.playerArg())
                .withPermission(tpaHereCmd.getPermission().base())
                .withFullDescription(tpaHereCmd.getDescription())
                .withShortDescription(tpaHereCmd.getDescription())
                .withUsage(tpaHereCmd.getUsage())
                .executesPlayer((sender, args) -> {
                    Player target = Bukkit.getPlayer(args.getRaw(0));

                    if (target == null || !target.isOnline()) {
                        MessageUtil.sendMessage(sender, MessageProvider.Errors.playerNotFound(), true);
                        return;
                    }

                    if (target.equals(sender)) {
                        MessageUtil.sendMessage(sender, MessageProvider.Errors.cantTeleportToYourself(), true);
                        return;
                    }

                    boolean requestExists = TpaRequests.getInstance().getTpaHereRequest(target, sender) != null;

                    if (requestExists) {
                        MessageUtil.sendMessage(sender, MessageProvider.Errors.requestAlreadySent(), true);
                        return;
                    }

                    TpaRequests.getInstance().addRequest(target, sender, TpaType.TPA_HERE);
                    MKernel.LOGGER.info(TpaRequests.getInstance().toString());

                    MessageUtil.sendMessage(sender, tpaHereCmd.getMessages()[1], true,
                            List.of("%target%"), List.of(target.getName()));
                    MessageUtil.sendMessage(target, tpaHereCmd.getMessages()[0], true,
                            List.of("%sender%"), List.of(sender.getName()));
                })
                .register();
    }
}
