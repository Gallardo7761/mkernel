package net.miarma.mkernel.common.minecraft.teleport;

import org.bukkit.entity.Player;

public record TpaRequest(Player from, Player to, TpaType type, long timestamp) {
    public TpaRequest(Player from, Player to, TpaType type) {
        this(from, to, type, System.currentTimeMillis());
    }
}
