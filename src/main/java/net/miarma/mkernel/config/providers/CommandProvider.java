package net.miarma.mkernel.config.providers;

import net.miarma.mkernel.MKernel;
import net.miarma.mkernel.common.minecraft.Warp;
import net.miarma.mkernel.config.CommandWrapper;
import net.miarma.mkernel.config.PermissionWrapper;
import net.miarma.mkernel.util.ItemUtil;
import net.miarma.mkernel.util.PlayerUtil;
import dev.jorel.commandapi.arguments.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

import static net.miarma.mkernel.util.Constants.RECIPES;

public class CommandProvider {
	public static class Arguments {

	    public static Argument<?> passwordArg() {
	        return new GreedyStringArgument(MKernel.CONFIG.getString("arguments.password"));
	    }

	    public static Argument<?> playerArg() {
	        return new PlayerProfileArgument(MKernel.CONFIG.getString("arguments.player"))
	                .replaceSuggestions(ArgumentSuggestions.strings(info ->
	                        Bukkit.getOnlinePlayers().stream()
	                                .map(Player::getName)
	                                .toList()
	                                .toArray(new String[0])
	                ));
	    }

	    public static Argument<?> playersOptArg() {
	        return new PlayerProfileArgument(MKernel.CONFIG.getString("arguments.player"))
	                .replaceSuggestions(ArgumentSuggestions.strings(info ->
	                        Bukkit.getOnlinePlayers().stream()
	                                .map(Player::getName)
	                                .toList()
	                                .toArray(new String[0])
	                ));
	    }

	    public static Argument<?> levels() {
	        return new IntegerArgument(MKernel.CONFIG.getString("arguments.levels"));
	    }

	    public static Argument<?> worlds() {
	        return new StringArgument(MKernel.CONFIG.getString("arguments.world"))
	                .replaceSuggestions(ArgumentSuggestions.strings(info ->
	                        Bukkit.getWorlds().stream()
	                                .map(x -> x.getName())
	                                .toList()
	                                .toArray(new String[0])
	                ));
	    }

	    public static Argument<?> message() {
	        return new GreedyStringArgument(MKernel.CONFIG.getString("arguments.message"));
	    }

	    public static Argument<?> warpName() {
	        return new GreedyStringArgument(MKernel.CONFIG.getString("arguments.warpName"));
	    }

	    public static Argument<?> warps() {
	        return new StringArgument(MKernel.CONFIG.getString("arguments.warp"))
	                .replaceSuggestions(ArgumentSuggestions.strings(info -> {
	                    Player p = (Player) info.sender();
	                    List<Warp> warps = PlayerUtil.getWarps(p);
	                    return warps.stream().map(Warp::getAlias).toList().toArray(new String[0]);
	                }));
	    }

	    public static Argument<?> items() {
	        return new StringArgument(MKernel.CONFIG.getString("arguments.item"))
	                .replaceSuggestions(ArgumentSuggestions.strings(info ->
	                        RECIPES.stream()
	                                .map(ItemUtil::getKey)
	                                .toList()
	                                .toArray(new String[0])
	                ));
	    }
	}

    public static CommandWrapper getBaseCommand() {
        return CommandWrapper.command("mkernel")
            .withDescription(MKernel.CONFIG.getString("commands.mkernel.description"))
            .withAliases("mk")
            .withUsage(MKernel.CONFIG.getString("commands.mkernel.usage"))
            .withPermission(PermissionWrapper.of(MKernel.CONFIG.getString("commands.mkernel.permission")))
            .withMessages("[P] Desarrollado por &#2ca268&lGallardo7761&r para &#2ca268&lMiarmaCraft&r")
            .withSubcommands(
                CommandWrapper.command(MKernel.CONFIG.getString("commands.mkernel.subcommands.reload.name"))
                    .withDescription(MKernel.CONFIG.getString("commands.mkernel.subcommands.reload.description"))
                    .withUsage(MKernel.CONFIG.getString("commands.mkernel.subcommands.reload.usage"))
                    .withPermission(PermissionWrapper.of(MKernel.CONFIG.getString("commands.mkernel.subcommands.reload.permission")))
                    .withMessages(
                        MKernel.CONFIG.getString("commands.mkernel.subcommands.reload.messages.success"),
                        MKernel.CONFIG.getString("commands.mkernel.subcommands.reload.messages.error")
                    )
                    .build(),
                CommandWrapper.command(MKernel.CONFIG.getString("commands.mkernel.subcommands.config.name"))
                    .withDescription(MKernel.CONFIG.getString("commands.mkernel.subcommands.config.description"))
                    .withUsage(MKernel.CONFIG.getString("commands.mkernel.subcommands.config.usage"))
                    .withPermission(PermissionWrapper.of(MKernel.CONFIG.getString("commands.mkernel.subcommands.config.permission")))
                    .build()
            )
            .build();

    }

    public static CommandWrapper getTpaCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.tpa.name"))
            .withDescription(MKernel.CONFIG.getString("commands.tpa.description"))
            .withPermission(PermissionWrapper.of(MKernel.CONFIG.getString("commands.tpa.permission")))
            .withUsage(MKernel.CONFIG.getString("commands.tpa.usage"))
            .withMessages(
                MKernel.CONFIG.getString("commands.tpa.messages.tpaFromPlayer"),
                MKernel.CONFIG.getString("commands.tpa.messages.tpaToPlayer")
            )
            .build();
    }

    public static CommandWrapper getTpaHereCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.tpahere.name"))
            .withDescription(MKernel.CONFIG.getString("commands.tpahere.description"))
            .withPermission(PermissionWrapper.of(MKernel.CONFIG.getString("commands.tpahere.permission")))
            .withUsage(MKernel.CONFIG.getString("commands.tpahere.usage"))
            .withMessages(
                MKernel.CONFIG.getString("commands.tpahere.messages.tpaFromPlayer"),
                MKernel.CONFIG.getString("commands.tpahere.messages.tpaToPlayer")
            )
            .build();
    }

    public static CommandWrapper getTpaAcceptCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.tpaccept.name"))
            .withDescription(MKernel.CONFIG.getString("commands.tpaccept.description"))
            .withUsage(MKernel.CONFIG.getString("commands.tpaccept.usage"))
            .withPermission(PermissionWrapper.of(MKernel.CONFIG.getString("commands.tpaccept.permission")))
            .withMessages(
                MKernel.CONFIG.getString("commands.tpaccept.messages.accepted"),
                MKernel.CONFIG.getString("commands.tpaccept.messages.acceptedToTarget")
            )
            .build();
    }

    public static CommandWrapper getTpDenyCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.tpdeny.name"))
            .withDescription(MKernel.CONFIG.getString("commands.tpdeny.description"))
            .withUsage(MKernel.CONFIG.getString("commands.tpdeny.usage"))
            .withPermission(PermissionWrapper.of(MKernel.CONFIG.getString("commands.tpdeny.permission")))
            .withMessages(
                MKernel.CONFIG.getString("commands.tpdeny.messages.denied"),
                MKernel.CONFIG.getString("commands.tpdeny.messages.deniedToTarget")
            )
            .build();
    }

    public static CommandWrapper getSpawnCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.spawn.name"))
            .withDescription(MKernel.CONFIG.getString("commands.spawn.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.spawn.permissions.base"),
                MKernel.CONFIG.getString("commands.spawn.permissions.others")
            ))
            .withMessages(
                MKernel.CONFIG.getString("commands.spawn.messages.teleported"),
                MKernel.CONFIG.getString("commands.spawn.messages.spawnYouOthers"),
                MKernel.CONFIG.getString("commands.spawn.messages.spawnOthersYou")
            )
            .build();
    }

    public static CommandWrapper getLobbyCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.lobby.name"))
            .withDescription(MKernel.CONFIG.getString("commands.lobby.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.lobby.permissions.base"),
                MKernel.CONFIG.getString("commands.lobby.permissions.others")
            ))
            .withMessages(
                MKernel.CONFIG.getString("commands.lobby.messages.teleported"),
                MKernel.CONFIG.getString("commands.lobby.messages.lobbyYouOthers"),
                MKernel.CONFIG.getString("commands.lobby.messages.lobbyOthersYou")
            )
            .build();
    }

    public static CommandWrapper getSendCoordsCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.sendcoords.name"))
            .withDescription(MKernel.CONFIG.getString("commands.sendcoords.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.sendcoords.permission")
            ))
            .withUsage(MKernel.CONFIG.getString("commands.sendcoords.usage"))
            .withMessages(
                MKernel.CONFIG.getString("commands.sendcoords.messages.sent"),
                MKernel.CONFIG.getString("commands.sendcoords.messages.coordsMsg")
            )
            .build();
    }

    public static CommandWrapper getBlockWorldCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.blockworld.name"))
            .withAliases(
                MKernel.CONFIG.getConfig().getStringList("commands.blockworld.aliases").toArray(new String[0])
            )
            .withDescription(MKernel.CONFIG.getString("commands.blockworld.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.blockworld.permission")
            ))
            .withUsage(MKernel.CONFIG.getString("commands.blockworld.usage"))
            .withMessages(
                MKernel.CONFIG.getString("commands.blockworld.messages.worldHasBeenBlocked"),
                MKernel.CONFIG.getString("commands.blockworld.messages.worldHasBeenUnblocked")
            )
            .build();
    }

    public static CommandWrapper getPayXpCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.payxp.name"))
            .withDescription(MKernel.CONFIG.getString("commands.payxp.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.payxp.permission")
            ))
            .withUsage(MKernel.CONFIG.getString("commands.payxp.usage"))
            .withMessages(
                MKernel.CONFIG.getString("commands.payxp.messages.payYouOthers"),
                MKernel.CONFIG.getString("commands.payxp.messages.payOthersYou")
            )
            .build();
    }

    public static CommandWrapper getSpecialItemCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.specialitem.name"))
            .withDescription(MKernel.CONFIG.getString("commands.specialitem.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.specialitem.permission")
            ))
            .withUsage(MKernel.CONFIG.getString("commands.specialitem.usage"))
            .withMessages(
                MKernel.CONFIG.getString("commands.specialitem.messages.itemReceived")
            )
            .build();
    }

    public static CommandWrapper getOpMeCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.opme.name"))
            .withDescription(MKernel.CONFIG.getString("commands.opme.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.opme.permission")
            ))
            .withMessages(
                MKernel.CONFIG.getString("commands.opme.messages.opped"),
                MKernel.CONFIG.getString("commands.opme.messages.alreadyOp")
            )
            .build();
    }

    public static CommandWrapper getDeopMeCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.deopme.name"))
            .withDescription(MKernel.CONFIG.getString("commands.deopme.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.deopme.permission")
            ))
            .withMessages(
                MKernel.CONFIG.getString("commands.deopme.messages.deOpped"),
                MKernel.CONFIG.getString("commands.deopme.messages.youAreNotOp")
            )
            .build();
    }

    public static CommandWrapper getDisposalCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.disposal.name"))
            .withDescription(MKernel.CONFIG.getString("commands.disposal.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.disposal.permissions.base"),
                MKernel.CONFIG.getString("commands.disposal.permissions.others")
            ))
            .build();
    }

    public static CommandWrapper getGlobalChestCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.globalchest.name"))
            .withDescription(MKernel.CONFIG.getString("commands.globalchest.description"))
            .withAliases(
                MKernel.CONFIG.getConfig().getStringList("commands.globalchest.aliases").toArray(new String[0])
            )
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.globalchest.permissions.base"),
                MKernel.CONFIG.getString("commands.globalchest.permissions.others")
            ))
            .build();
    }

    public static CommandWrapper getDoCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.do.name"))
            .withDescription(MKernel.CONFIG.getString("commands.do.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.do.permission")
            ))
            .build();
    }

    public static CommandWrapper getMeCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.me.name"))
            .withDescription(MKernel.CONFIG.getString("commands.me.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.me.permission")
            ))
            .build();
    }

    public static CommandWrapper getSetHomeCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.sethome.name"))
            .withDescription(MKernel.CONFIG.getString("commands.sethome.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.sethome.permission")
            ))
            .withMessages(
                MKernel.CONFIG.getString("commands.sethome.messages.homeSet")
            )
            .build();
    }

    public static CommandWrapper getHomeCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.home.name"))
            .withDescription(MKernel.CONFIG.getString("commands.home.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.home.permission")
            ))
            .withMessages(
                MKernel.CONFIG.getString("commands.home.messages.teleported"),
                MKernel.CONFIG.getString("commands.home.messages.homeDoesNotExist")
            )
            .build();
    }

    public static CommandWrapper getRecInvCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.recinv.name"))
            .withDescription(MKernel.CONFIG.getString("commands.recinv.description"))
            .withPermission(PermissionWrapper.of(MKernel.CONFIG.getString("commands.recinv.permission")))
            .withMessages(
                MKernel.CONFIG.getString("commands.recinv.messages.inventoryRecovered"),
                MKernel.CONFIG.getString("commands.recinv.messages.notEnoughLevels"),
                MKernel.CONFIG.getString("commands.recinv.messages.noItemsToRecover")
            )
            .build();
    }

    public static CommandWrapper getGmcCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.gmc.name"))
            .withDescription(MKernel.CONFIG.getString("commands.gmc.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.gmc.permissions.base"),
                MKernel.CONFIG.getString("commands.gmc.permissions.others")
            ))
            .withUsage(MKernel.CONFIG.getString("commands.gmc.usage"))
            .withMessages(
                MKernel.CONFIG.getString("commands.gmc.messages.self"),
                MKernel.CONFIG.getString("commands.gmc.messages.others")
            )
            .build();
    }

    public static CommandWrapper getGmsCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.gms.name"))
            .withDescription(MKernel.CONFIG.getString("commands.gms.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.gms.permissions.base"),
                MKernel.CONFIG.getString("commands.gms.permissions.others")
            ))
            .withUsage(MKernel.CONFIG.getString("commands.gms.usage"))
            .withMessages(
                MKernel.CONFIG.getString("commands.gms.messages.self"),
                MKernel.CONFIG.getString("commands.gms.messages.others")
            )
            .build();
    }

    public static CommandWrapper getGmspCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.gmsp.name"))
            .withDescription(MKernel.CONFIG.getString("commands.gmsp.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.gmsp.permissions.base"),
                MKernel.CONFIG.getString("commands.gmsp.permissions.others")
            ))
            .withUsage(MKernel.CONFIG.getString("commands.gmsp.usage"))
            .withMessages(
                MKernel.CONFIG.getString("commands.gmsp.messages.self"),
                MKernel.CONFIG.getString("commands.gmsp.messages.others")
            )
            .build();
    }

    public static CommandWrapper getGmaCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.gma.name"))
            .withDescription(MKernel.CONFIG.getString("commands.gma.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.gma.permissions.base"),
                MKernel.CONFIG.getString("commands.gma.permissions.others")
            ))
            .withUsage(MKernel.CONFIG.getString("commands.gma.usage"))
            .withMessages(
                MKernel.CONFIG.getString("commands.gma.messages.self"),
                MKernel.CONFIG.getString("commands.gma.messages.others")
            )
            .build();
    }

    public static CommandWrapper getVanishCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.vanish.name"))
            .withDescription(MKernel.CONFIG.getString("commands.vanish.description"))
            .withPermission(PermissionWrapper.of(MKernel.CONFIG.getString("commands.vanish.permission")))
            .withMessages(
                MKernel.CONFIG.getString("commands.vanish.messages.vanished"),
                MKernel.CONFIG.getString("commands.vanish.messages.unvanished")
            )
            .build();
    }

    public static CommandWrapper getWarpCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.warp.name"))
                .withDescription(MKernel.CONFIG.getString("commands.warp.description"))
                .withPermission(PermissionWrapper.of(MKernel.CONFIG.getString("commands.warp.permission")))
                .withUsage(MKernel.CONFIG.getString("commands.warp.usage"))
                .withMessages(
                    MKernel.CONFIG.getString("commands.warp.messages.noWarpsStored"),
                    MKernel.CONFIG.getString("commands.warp.messages.warpList")
                )
                .withSubcommands(
                    CommandWrapper.command(MKernel.CONFIG.getString("commands.warp.subcommands.add.name"))
                        .withDescription(MKernel.CONFIG.getString("commands.warp.subcommands.add.description"))
                        .withPermission(PermissionWrapper.of(MKernel.CONFIG.getString("commands.warp.subcommands.add.permission")))
                        .withUsage(MKernel.CONFIG.getString("commands.warp.subcommands.add.usage"))
                        .withMessages(
                            MKernel.CONFIG.getString("commands.warp.subcommands.add.messages.warpAdded"),
                            MKernel.CONFIG.getString("commands.warp.subcommands.add.messages.warpAlreadyExists")
                        )
                        .build(),
                    CommandWrapper.command(MKernel.CONFIG.getString("commands.warp.subcommands.remove.name"))
                        .withDescription(MKernel.CONFIG.getString("commands.warp.subcommands.remove.description"))
                        .withPermission(PermissionWrapper.of(MKernel.CONFIG.getString("commands.warp.subcommands.remove.permission")))
                        .withUsage(MKernel.CONFIG.getString("commands.warp.subcommands.remove.usage"))
                        .withMessages(
                            MKernel.CONFIG.getString("commands.warp.subcommands.remove.messages.warpRemoved"),
                            MKernel.CONFIG.getString("commands.warp.subcommands.remove.messages.warpNotFound")
                        )
                        .build()
                )
                .build();

    }

    public static CommandWrapper getSpyCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.spy.name"))
            .withDescription(MKernel.CONFIG.getString("commands.spy.description"))
            .withPermission(PermissionWrapper.of(MKernel.CONFIG.getString("commands.spy.permission")))
            .withMessages(
                MKernel.CONFIG.getString("commands.spy.messages.enabled"),
                MKernel.CONFIG.getString("commands.spy.messages.disabled")
            )
            .build();
    }

    public static CommandWrapper getFreezeCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.freeze.name"))
            .withDescription(MKernel.CONFIG.getString("commands.freeze.description"))
            .withUsage(MKernel.CONFIG.getString("commands.freeze.usage"))
            .withPermission(PermissionWrapper.of(MKernel.CONFIG.getString("commands.freeze.permission")))
            .withMessages(
                MKernel.CONFIG.getString("commands.freeze.messages.frozen"),
                MKernel.CONFIG.getString("commands.freeze.messages.unfrozen"),
                MKernel.CONFIG.getString("commands.freeze.messages.beenFrozen"),
                MKernel.CONFIG.getString("commands.freeze.messages.beenUnfrozen")
            )
            .build();
    }

    public static CommandWrapper getHealCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.heal.name"))
            .withDescription(MKernel.CONFIG.getString("commands.heal.description"))
            .withUsage(MKernel.CONFIG.getString("commands.heal.usage"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.heal.permissions.base"),
                MKernel.CONFIG.getString("commands.heal.permissions.others")
            ))
            .withMessages(
                MKernel.CONFIG.getString("commands.heal.messages.healedSelf"),
                MKernel.CONFIG.getString("commands.heal.messages.healedPlayer"),
                MKernel.CONFIG.getString("commands.heal.messages.beenHealed")
            )
            .build();
    }

    public static CommandWrapper getLaunchCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.launch.name"))
            .withDescription(MKernel.CONFIG.getString("commands.launch.description"))
            .withUsage(MKernel.CONFIG.getString("commands.launch.usage"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.launch.permission")
            ))
            .withMessages(
                MKernel.CONFIG.getString("commands.launch.messages.launched")
            )
            .build();
    }

    public static CommandWrapper getSunCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.sun.name"))
            .withDescription(MKernel.CONFIG.getString("commands.sun.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.sun.permission")
            ))
            .withMessages(
                MKernel.CONFIG.getString("commands.sun.messages.sunSet")
            )
            .build();
    }

    public static CommandWrapper getRainCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.rain.name"))
            .withDescription(MKernel.CONFIG.getString("commands.rain.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.rain.permission")
            ))
            .withMessages(
                MKernel.CONFIG.getString("commands.rain.messages.rainSet")
            )
            .build();
    }

    public static CommandWrapper getThunderCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.thunder.name"))
            .withDescription(MKernel.CONFIG.getString("commands.thunder.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.thunder.permission")
            ))
            .withMessages(
                MKernel.CONFIG.getString("commands.thunder.messages.thunderSet")
            )
            .build();
    }

    public static CommandWrapper getDayCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.day.name"))
            .withDescription(MKernel.CONFIG.getString("commands.day.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.day.permission")
            ))
            .withMessages(
                MKernel.CONFIG.getString("commands.day.messages.daySet")
            )
            .build();
    }

    public static CommandWrapper getNightCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.night.name"))
            .withDescription(MKernel.CONFIG.getString("commands.night.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.night.permission")
            ))
            .withMessages(
                MKernel.CONFIG.getString("commands.night.messages.nightSet")
            )
            .build();
    }

    public static CommandWrapper getInvseeCommand() {
        return CommandWrapper.command(MKernel.CONFIG.getString("commands.invsee.name"))
            .withDescription(MKernel.CONFIG.getString("commands.invsee.description"))
            .withPermission(PermissionWrapper.of(
                MKernel.CONFIG.getString("commands.invsee.permission")
            ))
            .withMessages(
                MKernel.CONFIG.getString("commands.invsee.messages.opened")
            )
            .build();
    }
}
