package net.miarma.mkernel.config;

public record PermissionWrapper(String base, String others) {
    public static PermissionWrapper of(String base) {
        return new PermissionWrapper(base, null);
    }
    public static PermissionWrapper of(String base, String others) {
        return new PermissionWrapper(base, others);
    }
}
