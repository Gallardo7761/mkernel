package net.miarma.mkernel.common.minecraft;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;

public class Warp implements Comparable<Warp> {
    private String alias;
    private double x;
    private double y;
    private double z;
    private String world;

    public Warp(String alias, double x, double y, double z, String world) {
        this.alias = alias;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public static Warp of(String alias, double x, double y, double z, String world) {
        return new Warp(alias, x, y, z, world);
    }

    public static Warp fromFile(FileConfiguration c, String alias) {
        return Warp.of(alias, c.getDouble(alias + ".x"),
                c.getDouble(alias + ".y"),
                c.getDouble(alias + ".z"),
                c.getString(alias + ".world"));
    }

    public static void toFile(FileConfiguration c, Warp warp) {
        c.set(warp.getAlias() + ".x", warp.getX());
        c.set(warp.getAlias() + ".y", warp.getY());
        c.set(warp.getAlias() + ".z", warp.getZ());
        c.set(warp.getAlias() + ".world", warp.getWorld());
    }

    public String getAlias() {
        return alias;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public String getWorld() {
        return world;
    }

    public String toString() {
        return "Alias: " + alias + ", X: " + x + ", Y: " + y + ", Z: " + z + ", World: " + world;
    }

    public String toFormattedMessage() {
        return "&b&l" + alias + ": &r\n" +
                "&aCoordenadas: &r" + x + ", " + y + ", " + z + "\n" +
                "&aMundo: &r" + world;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Warp warp = (Warp) o;
        return Double.compare(x, warp.x) == 0 && Double.compare(y, warp.y) == 0 && Double.compare(z, warp.z) == 0 && Objects.equals(world, warp.world);
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        result = 31 * result + Double.hashCode(z);
        result = 31 * result + Objects.hashCode(world);
        return result;
    }

    @Override
    public int compareTo(Warp o) {
        int res = Double.compare(x, o.x);
        if (res == 0) {
            res = Double.compare(y, o.y);
            if (res == 0) {
                res = Double.compare(z, o.z);
                if (res == 0) {
                    res = world.compareTo(o.world);
                }
            }
        }
        return res;
    }
}
