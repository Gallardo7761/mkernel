package net.miarma.mkernel.commands.misc;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import dev.jorel.commandapi.CommandAPICommand;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.providers.CommandProvider;
import net.miarma.mkernel.config.providers.CommandProvider.Arguments;
import net.miarma.mkernel.config.providers.MessageProvider;
import net.miarma.mkernel.util.MessageUtil;

public class DegreeCommand {
	public static void register() {
		CommandWrapper degreeCmd = CommandProvider.getDegreeCommand();
		new CommandAPICommand(degreeCmd.getName())
			.withOptionalArguments(CommandProvider.Arguments.titulaciones())
			.withFullDescription(degreeCmd.getDescription())
            .withPermission(degreeCmd.getPermission().base())
            .withShortDescription(degreeCmd.getDescription())
            .executesPlayer((sender, args) -> {
            	if (args.count() > 1) {
                    MessageUtil.sendMessage(sender, MessageProvider.Errors.tooManyArguments(), true);
                    return;
                }
            	
            	RegisteredServiceProvider<LuckPerms> provider = 
            			Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            	if (provider != null) {
            	    LuckPerms api = provider.getProvider();
            	    
            		if (args.count() == 0) {
            			api.getUserManager().loadUser(sender.getUniqueId())
        			        .thenCompose(user -> {
        			            String grupo = user.getNodes(NodeType.INHERITANCE).stream()
        			                    .map(InheritanceNode::getGroupName)
        			                    .findFirst().orElse("Inmigrante");
        			            return CompletableFuture.completedFuture(grupo);
        			        })
        			        .thenApply(grupo -> {
        			            MessageUtil.sendMessage(sender, degreeCmd.getMessages()[1], true,
        			                    List.of("%titulacion%"), List.of(grupo));
        			            return grupo;
        			        }).join();
                	} else {
                		String titulacion = args.getRaw(0).toLowerCase();
                		if (Arguments.TITULACIONES.stream()
                				.map(String::toLowerCase)
                				.noneMatch(t -> t.equals(titulacion))) {
                			MessageUtil.sendMessage(sender, degreeCmd.getMessages()[3], true);
                			return;
                		}
                		
                		api.getUserManager().loadUser(sender.getUniqueId())
	                	    .thenCompose(user -> {
	                	        DataMutateResult res = user.setPrimaryGroup(titulacion);
	
	                	        if (res != DataMutateResult.SUCCESS) {
	                	            MessageUtil.sendMessage(sender, degreeCmd.getMessages()[2], true);
	                	            return CompletableFuture.completedFuture(null);
	                	        }
	
	                	        return api.getUserManager().saveUser(user)
	                	            .thenRun(() -> {
	                	                MessageUtil.sendMessage(sender, degreeCmd.getMessages()[0], true,
	                	                        List.of("%titulacion%"), List.of(titulacion.toUpperCase()));
	                	            })
	                	            .exceptionally(ex -> {
	                	                MessageUtil.sendMessage(sender, degreeCmd.getMessages()[2], true);
	                	                ex.printStackTrace();
	                	                return null;
	                	            });
	                	    });
                	}
            	}
            })
            .register();
	}
}
