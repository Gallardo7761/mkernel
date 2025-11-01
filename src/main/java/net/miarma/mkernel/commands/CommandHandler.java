package net.miarma.mkernel.commands;

import net.miarma.mkernel.commands.admin.*;
import net.miarma.mkernel.commands.base.BaseCommand;
import net.miarma.mkernel.commands.misc.*;
import net.miarma.mkernel.commands.roleplay.DoCommand;
import net.miarma.mkernel.commands.roleplay.MeCommand;
import net.miarma.mkernel.commands.tp.TpDenyCommand;
import net.miarma.mkernel.commands.tp.TpaAcceptCommand;
import net.miarma.mkernel.commands.tp.TpaCommand;
import net.miarma.mkernel.commands.tp.TpaHereCommand;
import net.miarma.mkernel.commands.troll.LaunchCommand;
import dev.jorel.commandapi.CommandAPI;

public class CommandHandler {
    private static void registerCommands() {
        BaseCommand.register();
        TpaCommand.register();
        TpaAcceptCommand.register();
        TpDenyCommand.register();
        TpaHereCommand.register();
        CommandAPI.unregister("me");
        MeCommand.register();
        DoCommand.register();
        GlobalChestCommand.register();
        DisposalCommand.register();
        OpMeCommand.register();
        DeOpMeCommand.register();
        SpawnCommand.register();
        LobbyCommand.register();
        BlockWorldCommand.register();
        SpecialItemCommand.register();
        SendCoordsCommand.register();
        PayXpCommand.register();
        HomeCommand.register();
        SetHomeCommand.register();
        InventoryRecoveryCommand.register();
        GmcCommand.register();
        GmsCommand.register();
        GmspCommand.register();
        GmaCommand.register();
        VanishCommand.register();
        WarpCommand.register();
        SpyCommand.register();
        FreezeCommand.register();
        HealCommand.register();
        LaunchCommand.register();
        DayCommand.register();
        NightCommand.register();
        ThunderCommand.register();
        SunCommand.register();
        RainCommand.register();
        InvseeCommand.register();
        DegreeCommand.register();
    }

    public static void onEnable() {
        registerCommands();
    }
}
