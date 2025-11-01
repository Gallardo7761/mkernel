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

public class TpaCommand {
    public static void register() {
        CommandWrapper tpaCmd = CommandProvider.getTpaCommand();
        new CommandAPICommand(tpaCmd.getName())
                .withArguments(CommandProvider.Arguments.playerArg())
                .withPermission(tpaCmd.getPermission().base())
                .withFullDescription(tpaCmd.getDescription())
                .withShortDescription(tpaCmd.getDescription())
                .withUsage(tpaCmd.getUsage())
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

                    boolean requestExists = TpaRequests.getInstance().getTpaRequest(sender, target) != null;
                    if (requestExists) {
                        MessageUtil.sendMessage(sender, MessageProvider.Errors.requestAlreadySent(), true);
                        return;
                    }

                    /*boolean onCooldown = TpaRequests.getInstance().getCooldowns().containsKey(sender);
                    if(onCooldown) {
                        MessageUtils.sendMessage(sender, MessageProvider.Errors.cooldownHasNotExpired(), true,
                                List.of("%time%"), List.of(
                                        String.valueOf((ConfigProvider.Values.getTpCooldown() -
                                        (System.currentTimeMillis() -
                                        TpaRequests.getInstance().getCooldowns().get(sender))) / 1000)));
                        return;
                    }*/

                    TpaRequests.getInstance().addRequest(sender, target, TpaType.TPA);
                    MKernel.LOGGER.info(TpaRequests.getInstance().toString());

                    MessageUtil.sendMessage(sender, tpaCmd.getMessages()[1], true,
                            List.of("%target%"), List.of(target.getName()));
                    MessageUtil.sendMessage(target, tpaCmd.getMessages()[0], true,
                            List.of("%sender%"), List.of(sender.getName()));
                })
                .register();
    }
}
