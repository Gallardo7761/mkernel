package net.miarma.mkernel.commands.tp;

import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPICommand;
import net.miarma.mkernel.common.minecraft.teleport.TpaRequest;
import net.miarma.mkernel.common.minecraft.teleport.TpaRequests;
import net.miarma.mkernel.common.minecraft.teleport.TpaType;
import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.config.providers.MessageProvider;
import net.miarma.mkernel.util.MessageUtil;

public class TpaAcceptCommand {
    public static void register() {
        CommandWrapper tpaAcceptCmd = CommandProvider.getTpaAcceptCommand();
        new CommandAPICommand(tpaAcceptCmd.getName())
            .withOptionalArguments(CommandProvider.Arguments.playersOptArg())
            .withPermission(tpaAcceptCmd.getPermission().base())
            .withFullDescription(tpaAcceptCmd.getDescription())
            .withShortDescription(tpaAcceptCmd.getDescription())
            .withUsage(tpaAcceptCmd.getUsage())
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

                    if (request.type() == TpaType.TPA) {
                        request.from().teleport(sender.getLocation());
                    } else if (request.type() == TpaType.TPA_HERE) {
                        sender.teleport(request.to().getLocation());
                    }

                    MessageUtil.sendMessage(target, tpaAcceptCmd.getMessages()[1], true,
                            List.of("%sender%"), List.of(sender.getName()));
                    MessageUtil.sendMessage(sender, tpaAcceptCmd.getMessages()[0], true,
                            List.of("%target%"), List.of(target.getName()));

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

                    if (request.type() == TpaType.TPA) {
                        target.teleport(sender.getLocation());
                    } else if (request.type() == TpaType.TPA_HERE) {
                        sender.teleport(target.getLocation());
                    }

                    MessageUtil.sendMessage(target, tpaAcceptCmd.getMessages()[1], true,
                            List.of("%sender%"), List.of(sender.getName()));
                    MessageUtil.sendMessage(sender, tpaAcceptCmd.getMessages()[0], true,
                            List.of("%target%"), List.of(target.getName()));
                }
            })
            .register();
    }
}
