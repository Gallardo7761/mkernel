package net.miarma.mkernel.listeners;

import net.miarma.mkernel.common.annotation.Version;
import net.miarma.mkernel.config.providers.ConfigProvider;
import net.miarma.mkernel.common.minecraft.events.BlockEventHelper;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class CropsListener implements Listener {
    
    @EventHandler
    public void onHarvest(PlayerInteractEvent event) {    	
        if (!ConfigProvider.Modules.isHarvestOnRightClickEnabled()) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        BlockEventHelper.of(event.getPlayer(), block).handleCrop();
    }
}
