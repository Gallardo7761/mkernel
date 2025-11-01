package net.miarma.mkernel.config.providers;

import net.miarma.mkernel.MKernel;
import net.miarma.mkernel.config.WorldWrapper;
import net.miarma.mkernel.util.ConversionUtil;

public class ConfigProvider {
    public static class Permissions {
        public static String getChatFormatPermission() {
            return MKernel.CONFIG.getString("config.permissions.chatFormat");
        }
        public static String getAdminChatPermission() {
            return MKernel.CONFIG.getString("config.permissions.adminChat");
        }
        public static String getMentionsPermission() {
            return MKernel.CONFIG.getString("config.permissions.mentions");
        }
    }

    public static class Values {
        public static int getRottenFleshCookTime() {
            return MKernel.CONFIG.getInt("config.values.rottenFleshCookTime");
        }
        public static int getZombificationPotionTime() {
            return MKernel.CONFIG.getInt("config.values.zombificationPotionTime");
        }
        public static int getSpawnerBreakerProbability() {
            return MKernel.CONFIG.getInt("config.values.spawnerBreakerProbability");
        }
        public static int getRecInvRequiredLevel() {
            return MKernel.CONFIG.getInt("config.values.recInvRequiredLevel");
        }
        public static float getXpLossOnDeath() {
            return MKernel.CONFIG.getConfig().getFloat("config.values.xpLossOnDeath");
        }
        public static long getTpCooldown() {
            return ConversionUtil.cooldownToMillis(MKernel.CONFIG.getString("config.values.tpCooldown"));
        }
        public static int getMaxWarps() {
            return MKernel.CONFIG.getInt("config.values.maxWarps");
        }
        public static int getRecInvPlayerRadius() {
            return MKernel.CONFIG.getInt("config.values.recInvPlayerRadius");
        }
        public static int getRecInvSpawnDistance() {
            return MKernel.CONFIG.getInt("config.values.recInvSpawnDistance");
        }
    }

    public static class Modules {
        public static boolean isJoinTitleEnabled() {
            return MKernel.CONFIG.getBoolean("config.modules.joinTitle");
        }
        public static boolean isLeaveTitleEnabled() {
            return MKernel.CONFIG.getBoolean("config.modules.leaveTitle");
        }
        public static boolean isDeathTitleEnabled() {
            return MKernel.CONFIG.getBoolean("config.modules.deathTitle");
        }
        public static boolean isHarvestOnRightClickEnabled() {
            return MKernel.CONFIG.getBoolean("config.modules.harvestOnRightClick");
        }
        public static boolean isAutoItemRefillEnabled() {
            return MKernel.CONFIG.getBoolean("config.modules.autoItemRefill");
        }
        public static boolean isAdminChatEnabled() {
            return MKernel.CONFIG.getBoolean("config.modules.adminChat");
        }
        public static boolean isChatFormatEnabled() {
            return MKernel.CONFIG.getBoolean("config.modules.chatFormat");
        }
        public static boolean isMentionsEnabled() {
            return MKernel.CONFIG.getBoolean("config.modules.mentions");
        }
        public static boolean isBookColorsEnabled() {
            return MKernel.CONFIG.getBoolean("config.modules.bookColors");
        }
        public static boolean isSpawnAtLobbyEnabled() {
            return MKernel.CONFIG.getBoolean("config.modules.spawnAtLobby");
        }
        public static boolean isRecoverInventoryEnabled() {
            return MKernel.CONFIG.getBoolean("config.modules.recoverInventory");
        }
    }

    public static class Worlds {
        public static WorldWrapper getLobby() {
            String name = MKernel.CONFIG.getString("config.worlds.lobby.name");
            double x = MKernel.CONFIG.getConfig().getDouble("config.worlds.lobby.x");
            double y = MKernel.CONFIG.getConfig().getDouble("config.worlds.lobby.y");
            double z = MKernel.CONFIG.getConfig().getDouble("config.worlds.lobby.z");
            int yaw = MKernel.CONFIG.getConfig().getInt("config.worlds.lobby.yaw");
            int pitch = MKernel.CONFIG.getConfig().getInt("config.worlds.lobby.pitch");
            return new WorldWrapper(name, x, y, z, yaw, pitch);
        }
    }
}