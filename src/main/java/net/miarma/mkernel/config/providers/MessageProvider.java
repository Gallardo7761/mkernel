package net.miarma.mkernel.config.providers;

import net.miarma.mkernel.MKernel;

public class MessageProvider {
    public static String getPrefix() {
        return MKernel.CONFIG.getString("language.prefix");
    }
    public static String getAdminPrefix() {
        return MKernel.CONFIG.getString("language.adminPrefix");
    }

    public static class Inventories {
        public static String getGlobalChestTitle() {
            return MKernel.CONFIG.getString("language.inventories.globalChest.title");
        }
        public static String getDisposalTitle() {
            return MKernel.CONFIG.getString("language.inventories.disposal.title");
        }
        public static String getInvseeTitle() {
            return MKernel.CONFIG.getString("language.inventories.invsee.title");
        }
        public static String getConfigMenuTitle() {
            return MKernel.CONFIG.getString("language.inventories.configMenu.title");
        }
        public static String getConfigMenuValueName() {
            return MKernel.CONFIG.getString("language.inventories.configMenu.valueName");
        }
        public static String getConfigMenuValueLore() {
            return MKernel.CONFIG.getString("language.inventories.configMenu.valueLore");
        }
    }

    public static class Titles {
        public static String getTitleFormat() {
            return MKernel.CONFIG.getString("language.titles.titleFormat");
        }
        public static String getJoinSubtitle() {
            return MKernel.CONFIG.getString("language.titles.subtitles.join");
        }
        public static String getLeaveSubtitle() {
            return MKernel.CONFIG.getString("language.titles.subtitles.leave");
        }
        public static String getDeathSubtitle() {
            return MKernel.CONFIG.getString("language.titles.subtitles.death");
        }
    }

    public static class Events {
        public static String getOnMentionFormat() {
            return MKernel.CONFIG.getString("language.events.onMention.format");
        }
        public static String getOnMentionMessage() {
            return MKernel.CONFIG.getString("language.events.onMention.youWereMentioned");
        }
        public static String getOnDeathLostLevelsItems() {
            return MKernel.CONFIG.getString("language.events.onDeath.lostLevelsItems");
        }
        public static String getOnDeathItemsNotRecovered() {
            return MKernel.CONFIG.getString("language.events.onDeath.itemsNotRecovered");
        }
        public static String getOnCommandSpyMessage() {
            return MKernel.CONFIG.getString("language.events.onCommand.spyMessage");
        }
    }

    public static class Items {
        public static String getScissorsName() {
            return MKernel.CONFIG.getString("language.items.scissors.name");
        }
        public static String getScissorsLore() {
            return MKernel.CONFIG.getString("language.items.scissors.lore");
        }
        public static String getAdminStickName() {
            return MKernel.CONFIG.getString("language.items.adminStick.name");
        }
        public static String getAdminStickLore() {
            return MKernel.CONFIG.getString("language.items.adminStick.lore");
        }
        public static String getSpawnerBreakerName() {
            return MKernel.CONFIG.getString("language.items.spawnerBreaker.name");
        }
        public static String getSpawnerBreakerLore() {
            return MKernel.CONFIG.getString("language.items.spawnerBreaker.lore");
        }
        public static String getZombificationPotionName() {
            return MKernel.CONFIG.getString("language.items.zombificationPotion.name");
        }
        public static String getZombificationPotionLore() {
            return MKernel.CONFIG.getString("language.items.zombificationPotion.lore");
        }
    }

    public static class Errors {
        public static String failedCreatingLangs() {
            return MKernel.CONFIG.getString("language.errors.langs");
        }
        public static String notConsoleCommand() {
            return MKernel.CONFIG.getString("language.errors.notConsoleCommand");
        }
        public static String onlyPlayerCommand() {
            return MKernel.CONFIG.getString("language.errors.onlyPlayerCommand");
        }
        public static String playerNotFound() {
            return MKernel.CONFIG.getString("language.errors.playerNotFound");
        }
        public static String cantTeleportToYourself() {
            return MKernel.CONFIG.getString("language.errors.cantTeleportToYourself");
        }
        public static String requestAlreadySent() {
            return MKernel.CONFIG.getString("language.errors.requestAlreadySent");
        }
        public static String noRequestFound() {
            return MKernel.CONFIG.getString("language.errors.noRequestFound");
        }
        public static String noPermission() {
            return MKernel.CONFIG.getString("language.errors.noPermission");
        }
        public static String tooManyArguments() {
            return MKernel.CONFIG.getString("language.errors.tooManyArguments");
        }
        public static String playerRequired() {
            return MKernel.CONFIG.getString("language.errors.playerRequired");
        }
        public static String invalidArgument() {
            return MKernel.CONFIG.getString("language.errors.invalidArgument");
        }
        public static String temporarilyDisabled() {
            return MKernel.CONFIG.getString("language.errors.temporarilyDisabled");
        }
        public static String itemNotFound() {
            return MKernel.CONFIG.getString("language.errors.itemNotFound");
        }
        public static String lobbyDoesNotExist() {
            return MKernel.CONFIG.getString("language.errors.lobbyDoesNotExist");
        }
        public static String worldIsBlocked() {
            return MKernel.CONFIG.getString("language.errors.worldIsBlocked");
        }
        public static String notEnoughLevels() {
            return MKernel.CONFIG.getString("language.errors.notEnoughLevels");
        }
        public static String cooldownHasNotExpired() {
            return MKernel.CONFIG.getString("language.errors.cooldownHasNotExpired");
        }
        public static String maxWarpsReached() {
            return MKernel.CONFIG.getString("language.errors.maxWarpsReached");
        }
    }
}
