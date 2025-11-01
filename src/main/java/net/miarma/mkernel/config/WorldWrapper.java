package net.miarma.mkernel.config;

public record WorldWrapper(String name, double x, double y, double z, int yaw, int pitch) {
    public WorldWrapper {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
    }
}
