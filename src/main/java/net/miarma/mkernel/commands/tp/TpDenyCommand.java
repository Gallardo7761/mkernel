package net.miarma.mkernel.commands.tp;

import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPICommand;
import net.miarma.mkernel.common.minecraft.teleport.TpaRequest;
import net.miarma.mkernel.common.minecraft.teleport.TpaRequests;
import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.config.providers.MessageProvider;
import net.miarma.mkernel.util.MessageUtil;

public class TpDenyCommand {
    public static void register() {
        CommandWrapper tpDenyCmd = CommandProvider.getTpDenyCommand();
        new CommandAPICommand(tpDenyCmd.getName())
            .withOptionalArguments(CommandProvider.Arguments.playersOptArg())
            .withPermission(tpDenyCmd.getPermission().base())
            .withFullDescription(tpDenyCmd.getDescription())
            .withShortDescription(tpDenyCmd.getDescription())
            .withUsage(tpDenyCmd.getUsage())
            .executesPlayer((sender, args) -> {
                if(args.count() == 0) {
                    Optional<TpaRequest> optTpaRequest = TpaRequests.getInstance().getRequests().stream()
                            .filter(r -> r.from().equals(sender))
                            .findFirst();

                    Optional<TpaRequest> optTpaHereRequest = TpaRequests.getInstance().getRequests().stream()
                            .filter(r -> r.to().equals(sender))
                            .findFirst();

                    if(optTpaRequest.isEmpty() && optTpaHereRequest.isEmpty()) {
                        MessageUtil.sendMessage(sender, MessageProvider.Errors.noRequestFound(), true);
                        return;
                    }

                    TpaRequest request;
                    request = optTpaRequest.orElseGet(optTpaHereRequest::get);
                    TpaRequests.getInstance().removeRequest(request);

                    Player target = Bukkit.getPlayer(request.from().getName());

                    MessageUtil.sendMessage(sender, tpDenyCmd.getMessages()[0], true,
                            List.of("%target%"), List.of(target.getName()));
                    MessageUtil.sendMessage(target, tpDenyCmd.getMessages()[1], true,
                            List.of("%sender%"), List.of(sender.getName()));

                } else {
                    Player target = Bukkit.getPlayer(args.getRaw(0));

                    if (target == null || !target.isOnline()) {
                        MessageUtil.sendMessage(sender, MessageProvider.Errors.playerNotFound(), true);
                        return;
                    }

                    TpaRequest request;
                    if(TpaRequests.getInstance().getTpaRequest(target, sender) != null) {
                        request = TpaRequests.getInstance().getTpaRequest(target, sender);
                    } else {
                        request = TpaRequests.getInstance().getTpaHereRequest(sender, target);
                    }

                    if (request == null) {
                        MessageUtil.sendMessage(sender, MessageProvider.Errors.noRequestFound(), true);
                        return;
                    }

                    TpaRequests.getInstance().removeRequest(request);

                    MessageUtil.sendMessage(sender, tpDenyCmd.getMessages()[0], true,
                            List.of("%target%"), List.of(target.getName()));
                    MessageUtil.sendMessage(target, tpDenyCmd.getMessages()[1], true,
                            List.of("%sender%"), List.of(sender.getName()));
                }
            })
            .register();
    }
}
