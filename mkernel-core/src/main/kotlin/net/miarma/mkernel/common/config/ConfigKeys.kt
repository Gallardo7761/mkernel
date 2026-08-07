package net.miarma.mkernel.common.config

object ConfigKeys {

    object Modules {
        object Core {
            const val MAIN = "modules.core"
            object Commands {
                const val DISPOSAL = "modules.core.commands.disposal"
                const val GLOBAL_CHEST = "modules.core.commands.globalchest"
                const val PAY_XP = "modules.core.commands.payxp"
                const val SEND_COORDS = "modules.core.commands.sendcoords"
                const val NICK = "modules.core.commands.nick"
            }
        }

        object Admin {
            const val MAIN = "modules.admin"
            const val CHAT = "modules.admin.chat"
            object Commands {
                const val SPECIAL_ITEM = "modules.admin.commands.specialItem"
                const val INVSEE = "modules.admin.commands.invsee"
                const val GMA = "modules.admin.commands.gma"
                const val GMSP = "modules.admin.commands.gmsp"
                const val GMC = "modules.admin.commands.gmc"
                const val GMS = "modules.admin.commands.gms"
                const val HEAL = "modules.admin.commands.heal"
                const val OPME = "modules.admin.commands.opme"
                const val DEOPME = "modules.admin.commands.deopme"
                const val SPY = "modules.admin.commands.spy"
                const val VANISH = "modules.admin.commands.vanish"
                const val SEQUENCE = "modules.admin.commands.sequence"
                const val FLYSPEED = "modules.admin.commands.flyspeed"
                const val FREEZE = "modules.admin.commands.freeze"
            }
        }

        object Chat {
            const val MAIN = "modules.chat"
            const val FORMAT = "modules.chat.format"
            const val MENTIONS = "modules.chat.mentions"
            const val ROLEPLAY = "modules.chat.roleplay"
            const val ENDERMAN_ANGER = "modules.chat.endermanAnger"
        }

        object Shop {
            const val MAIN = "modules.shop"
        }

        object Teleport {
            const val MAIN = "modules.teleport"
            const val SPAWN_AT_LOBBY = "modules.teleport.spawnAtLobby"
        }

        object Player {
            const val MAIN = "modules.player"
            const val JOIN_TITLE = "modules.player.joinTitle"
            const val LEAVE_TITLE = "modules.player.leaveTitle"
            const val DEATH_TITLE = "modules.player.deathTitle"
            const val RECOVER_INVENTORY = "modules.player.recoverInventory"
        }

        object World {
            const val MAIN = "modules.world"
            const val HARVEST_RIGHT_CLICK = "modules.world.harvestRightClick"
            const val AUTO_ITEM_REFILL = "modules.world.autoItemRefill"
            const val NO_NETHER_PORTALS = "modules.world.noNetherPortals"
            const val TIME_WEATHER_CONTROL = "modules.world.timeWeatherControl"
        }
    }

    object Settings {
        object Shops {
            const val TAX_PERCENT = "settings.shops.taxPercent"
            const val SERVER_ACCOUNT = "settings.shops.serverAccount"
            const val PERM_ADMIN = "settings.shops.permissions.admin"
        }

        object Teleport {
            const val COOLDOWN = "settings.teleport.tpCooldown"
            const val MAX_WARPS = "settings.teleport.maxWarps"
        }

        object Death {
            const val XP_LOSS = "settings.death.xpLossOnDeath"
            const val REC_INV_LEVEL = "settings.death.recoverInventory.requiredLevel"
            const val REC_INV_RADIUS = "settings.death.recoverInventory.playerRadius"
            const val REC_INV_SPAWN_DIST = "settings.death.recoverInventory.spawnDistance"
        }

        object Chat {
            const val PREFIX = "settings.chat.prefix"
            const val PUBLIC_FORMAT = "settings.chat.publicFormat"
            const val PERM_FORMAT = "settings.chat.permissions.format"
            const val PERM_MENTIONS = "settings.chat.permissions.mentions"

            object Admin {
                const val TRIGGER = "settings.chat.adminChat.trigger"
                const val FORMAT = "settings.chat.adminChat.format"
                const val PERM = "settings.chat.adminChat.permission"
            }
        }

        object Blacklist {
            const val NICKS = "blacklist.nicks"
            const val COMMANDS = "blacklist.commands"
            const val RECIPES = "blacklist.recipes"
        }

        object Worlds {
            const val LOBBY_NAME = "settings.worlds.lobby.name"
            const val LOBBY_X = "settings.worlds.lobby.coords.x"
            const val LOBBY_Y = "settings.worlds.lobby.coords.y"
            const val LOBBY_Z = "settings.worlds.lobby.coords.z"
            const val LOBBY_YAW = "settings.worlds.lobby.coords.yaw"
            const val LOBBY_PITCH = "settings.worlds.lobby.coords.pitch"
        }

        const val SEQUENCES = "settings.sequentialCommands"
    }

    object Arguments {
        const val PLAYER = "arguments.player"
        const val LEVELS = "arguments.levels"
        const val WORLD = "arguments.world"
        const val MESSAGE = "arguments.message"
        const val ITEM = "arguments.item"
        const val WARP_NAME = "arguments.warpName"
        const val SPEED = "arguments.speed"
        const val SEQUENCE = "arguments.sequence"
        const val NICKNAME = "arguments.nickname"
    }

    object Messages {
        object General {
            const val UNKNOWN = "general.unknown"

            object Titles {
                const val FORMAT = "general.titles.format"
                const val JOIN = "general.titles.join"
                const val LEAVE = "general.titles.leave"
                const val DEATH = "general.titles.death"
            }

            object Errors {
                const val NOT_CONSOLE_COMMAND = "general.errors.notConsoleCommand"
                const val ONLY_PLAYER_COMMAND = "general.errors.onlyPlayerCommand"
                const val PLAYER_NOT_FOUND = "general.errors.playerNotFound"
                const val NO_PERMISSION = "general.errors.noPermission"
                const val TOO_MANY_ARGUMENTS = "general.errors.tooManyArguments"
                const val PLAYER_REQUIRED = "general.errors.playerRequired"
                const val INVALID_ARGUMENT = "general.errors.invalidArgument"
                const val TEMPORARILY_DISABLED = "general.errors.temporarilyDisabled"
                const val NOT_A_NUMBER = "general.errors.notANumber"
                const val NOT_ENOUGH_LEVELS = "general.errors.notEnoughLevels"
            }
        }

        object Inventories {
            const val GLOBAL_CHEST_TITLE = "inventories.globalChestTitle"
            const val DISPOSAL_TITLE = "inventories.disposalTitle"
            const val INVSEE_TITLE = "inventories.invseeTitle"

            const val CONFIG_TITLE = "inventories.configMenu.title"
            const val CONFIG_SUBTITLE = "inventories.configMenu.subTitle"
            const val CONFIG_VAL_NAME = "inventories.configMenu.valueName"
            const val CONFIG_VAL_LORE = "inventories.configMenu.valueLore"

            const val CONFIG_MODULE_NAME = "inventories.configMenu.moduleName"
            const val CONFIG_MODULE_LORE_STATE = "inventories.configMenu.moduleLoreState"
            const val CONFIG_MODULE_LORE_HINT = "inventories.configMenu.moduleLoreHint"

            const val CONFIG_STATE_ENABLED_COLOR = "inventories.configMenu.stateEnabledColor"
            const val CONFIG_STATE_DISABLED_COLOR = "inventories.configMenu.stateDisabledColor"
            const val CONFIG_STATE_ENABLED_TEXT = "inventories.configMenu.stateEnabledText"
            const val CONFIG_STATE_DISABLED_TEXT = "inventories.configMenu.stateDisabledText"

            const val CONFIG_TOGGLE_NAME = "inventories.configMenu.toggleName"
            const val CONFIG_TOGGLE_STATE_ENABLED = "inventories.configMenu.toggleStateEnabled"
            const val CONFIG_TOGGLE_STATE_DISABLED = "inventories.configMenu.toggleStateDisabled"
            const val CONFIG_TOGGLE_LORE = "inventories.configMenu.toggleLore"

            const val CONFIG_ACTION_ENABLE = "inventories.configMenu.actionEnable"
            const val CONFIG_ACTION_DISABLE = "inventories.configMenu.actionDisable"

            const val CONFIG_BACK_NAME = "inventories.configMenu.backName"
        }

        object Core {
            object _Meta {
                const val NAME = "modules.core._meta.name"
                const val ICON = "modules.core._meta.icon"
            }
        }

        object Connection {
            object _Meta {
                const val NAME = "modules.connection._meta.name"
                const val ICON = "modules.connection._meta.icon"
            }
            const val JOIN = "modules.connection.joinMessage"
            const val LEAVE = "modules.connection.leaveMessage"
        }

        object Chat {
            object _Meta {
                const val NAME = "modules.chat._meta.name"
                const val ICON = "modules.chat._meta.icon"
            }
            const val MENTIONS_FORMAT = "modules.chat.mentionFormat"
            const val YOU_WERE_MENTIONED = "modules.chat.youWereMentioned"
        }

        object Shops {
            object _Meta {
                const val NAME = "modules.shops._meta.name"
                const val ICON = "modules.shops._meta.icon"
            }
            object Hologram {
                const val TITLE = "modules.shops.hologram.title"
                const val PRICE = "modules.shops.hologram.price"
                const val STOCK = "modules.shops.hologram.stock"
            }
            object Inventory {
                const val TITLE = "modules.shops.inventory.title"
                const val ITEM_LORE = "modules.shops.inventory.itemLore"
            }
            object Chat {
                const val PLACED = "modules.shops.chat.placed"
                const val CREATED = "modules.shops.chat.created"
                const val BUY = "modules.shops.chat.buy"
                const val SOLD = "modules.shops.chat.sold"
                const val DESTROY = "modules.shops.chat.destroy"
            }
            object Errors {
                const val NO_STOCK = "modules.shops.errors.noStock"
                const val NO_ECONOMY = "modules.shops.errors.noEconomy"
                const val NOT_ENOUGH_MONEY = "modules.shops.errors.notEnoughMoney"
                const val SHOP_NOT_ACCESSIBLE = "modules.shops.errors.shopNotAccessible"
                const val ERROR_BUYING = "modules.shops.errors.errorBuyingItem"
                const val INVENTORY_FULL = "modules.shops.errors.inventoryFull"
                const val CANT_BUY_OWN = "modules.shops.errors.cantBuyOwn"
            }
        }

        object Teleport {
            object _Meta {
                const val NAME = "modules.teleport._meta.name"
                const val ICON = "modules.teleport._meta.icon"
            }
            object Errors {
                const val CANT_TP_SELF = "modules.teleport.errors.cantTeleportToYourself"
                const val REQ_ALREADY_SENT = "modules.teleport.errors.requestAlreadySent"
                const val NO_REQ_FOUND = "modules.teleport.errors.noRequestFound"
                const val LOBBY_NOT_EXIST = "modules.teleport.errors.lobbyDoesNotExist"
                const val COOLDOWN = "modules.teleport.errors.cooldownHasNotExpired"
                const val MAX_WARPS = "modules.teleport.errors.maxWarpsReached"
                const val NO_LAST_POS = "modules.teleport.errors.noLastPosition"
            }
        }

        object Death {
            object _Meta {
                const val NAME = "modules.death._meta.name"
                const val ICON = "modules.death._meta.icon"
            }
            const val LOST_LEVELS_ITEMS = "modules.death.chat.lostLevelsItems"
            const val ITEMS_NOT_RECOVERED = "modules.death.chat.itemsNotRecovered"
            object Errors {
                const val NOT_ENOUGH_LEVELS = "modules.death.errors.notEnoughLevels"
                const val NO_ITEMS = "modules.death.errors.noItemsToRecover"
            }
        }

        object Admin {
            object _Meta {
                const val NAME = "modules.admin._meta.name"
                const val ICON = "modules.admin._meta.icon"
            }
            const val SPY_MESSAGE = "modules.admin.spyMessage"
            const val WHILE_FROZEN = "modules.admin.whileFrozen"
            object Errors {
                const val CANNOT_FREEZE_SELF = "modules.admin.errors.cannotFreezeSelf"
                const val WORLD_BLOCKED = "modules.admin.errors.worldIsBlocked"
            }
        }

        object Player {
            object _Meta {
                const val NAME = "modules.player._meta.name"
                const val ICON = "modules.player._meta.icon"
            }
        }

        object Misc {
            object Errors {
                const val ITEM_NOT_FOUND = "modules.misc.errors.itemNotFound"
                const val NICK_BLACKLISTED = "modules.misc.errors.nickBlacklisted"
            }
        }

        object World {
            object _Meta {
                const val NAME = "modules.world._meta.name"
                const val ICON = "modules.world._meta.icon"
            }
            const val ILLEGAL_PORTAL = "modules.world.illegalPortal"
        }
    }

    object Commands {
        object MKernel {
            const val NAME = "commands.mkernel.name"
            const val DESC = "commands.mkernel.description"
            const val PERM = "commands.mkernel.permission"
            const val USAGE = "commands.mkernel.usage"
            object Reload {
                const val NAME = "commands.mkernel.subcommands.reload.name"
                const val DESC = "commands.mkernel.subcommands.reload.description"
                const val PERM = "commands.mkernel.subcommands.reload.permission"
                const val USAGE = "commands.mkernel.subcommands.reload.usage"
                const val MSG_SUCCESS = "commands.mkernel.subcommands.reload.messages.success"
                const val MSG_ERROR = "commands.mkernel.subcommands.reload.messages.error"
            }
            object Config {
                const val NAME = "commands.mkernel.subcommands.config.name"
                const val DESC = "commands.mkernel.subcommands.config.description"
                const val PERM = "commands.mkernel.subcommands.config.permission"
                const val USAGE = "commands.mkernel.subcommands.config.usage"
            }
        }

        object Tpa {
            const val NAME = "commands.tpa.name"
            const val DESC = "commands.tpa.description"
            const val PERM = "commands.tpa.permission"
            const val USAGE = "commands.tpa.usage"
            const val MSG_FROM = "commands.tpa.messages.tpaFromPlayer"
            const val MSG_TO = "commands.tpa.messages.tpaToPlayer"
        }

        object TpaHere {
            const val NAME = "commands.tpahere.name"
            const val DESC = "commands.tpahere.description"
            const val PERM = "commands.tpahere.permission"
            const val USAGE = "commands.tpahere.usage"
            const val MSG_FROM = "commands.tpahere.messages.tpaFromPlayer"
            const val MSG_TO = "commands.tpahere.messages.tpaToPlayer"
        }

        object TpAccept {
            const val NAME = "commands.tpaccept.name"
            const val DESC = "commands.tpaccept.description"
            const val PERM = "commands.tpaccept.permission"
            const val USAGE = "commands.tpaccept.usage"
            const val MSG_ACCEPTED = "commands.tpaccept.messages.accepted"
            const val MSG_ACCEPTED_TARGET = "commands.tpaccept.messages.acceptedToTarget"
        }

        object TpDeny {
            const val NAME = "commands.tpdeny.name"
            const val DESC = "commands.tpdeny.description"
            const val PERM = "commands.tpdeny.permission"
            const val USAGE = "commands.tpdeny.usage"
            const val MSG_DENIED = "commands.tpdeny.messages.denied"
            const val MSG_DENIED_TARGET = "commands.tpdeny.messages.deniedToTarget"
        }

        object Spawn {
            const val NAME = "commands.spawn.name"
            const val DESC = "commands.spawn.description"
            const val PERM_BASE = "commands.spawn.permissions.base"
            const val PERM_OTHERS = "commands.spawn.permissions.others"
            const val MSG_TELEPORTED = "commands.spawn.messages.teleported"
            const val MSG_YOU_OTHERS = "commands.spawn.messages.spawnYouOthers"
            const val MSG_OTHERS_YOU = "commands.spawn.messages.spawnOthersYou"
        }

        object Lobby {
            const val NAME = "commands.lobby.name"
            const val DESC = "commands.lobby.description"
            const val PERM_BASE = "commands.lobby.permissions.base"
            const val PERM_OTHERS = "commands.lobby.permissions.others"
            const val MSG_TELEPORTED = "commands.lobby.messages.teleported"
            const val MSG_YOU_OTHERS = "commands.lobby.messages.lobbyYouOthers"
            const val MSG_OTHERS_YOU = "commands.lobby.messages.lobbyOthersYou"
        }

        object SendCoords {
            const val NAME = "commands.sendcoords.name"
            const val DESC = "commands.sendcoords.description"
            const val PERM = "commands.sendcoords.permission"
            const val USAGE = "commands.sendcoords.usage"
            const val MSG_SENT = "commands.sendcoords.messages.sent"
            const val MSG_COORDS = "commands.sendcoords.messages.coordsMsg"
        }

        object BlockWorld {
            const val NAME = "commands.blockworld.name"
            const val ALIASES = "commands.blockworld.aliases"
            const val DESC = "commands.blockworld.description"
            const val PERM = "commands.blockworld.permission"
            const val USAGE = "commands.blockworld.usage"
            const val MSG_BLOCKED = "commands.blockworld.messages.worldHasBeenBlocked"
            const val MSG_UNBLOCKED = "commands.blockworld.messages.worldHasBeenUnblocked"
        }

        object PayXp {
            const val NAME = "commands.payxp.name"
            const val DESC = "commands.payxp.description"
            const val PERM = "commands.payxp.permission"
            const val USAGE = "commands.payxp.usage"
            const val MSG_YOU_OTHERS = "commands.payxp.messages.payYouOthers"
            const val MSG_OTHERS_YOU = "commands.payxp.messages.payOthersYou"
        }

        object SpecialItem {
            const val NAME = "commands.specialitem.name"
            const val DESC = "commands.specialitem.description"
            const val PERM = "commands.specialitem.permission"
            const val USAGE = "commands.specialitem.usage"
            const val MSG_RECEIVED = "commands.specialitem.messages.itemReceived"
        }

        object OpMe {
            const val NAME = "commands.opme.name"
            const val DESC = "commands.opme.description"
            const val PERM = "commands.opme.permission"
            const val MSG_OPPED = "commands.opme.messages.opped"
            const val MSG_ALREADY = "commands.opme.messages.alreadyOp"
        }

        object DeOpMe {
            const val NAME = "commands.deopme.name"
            const val DESC = "commands.deopme.description"
            const val PERM = "commands.deopme.permission"
            const val MSG_DEOPPED = "commands.deopme.messages.deOpped"
            const val MSG_NOT_OP = "commands.deopme.messages.youAreNotOp"
        }

        object Disposal {
            const val NAME = "commands.disposal.name"
            const val DESC = "commands.disposal.description"
            const val PERM_BASE = "commands.disposal.permissions.base"
            const val PERM_OTHERS = "commands.disposal.permissions.others"
        }

        object GlobalChest {
            const val NAME = "commands.globalchest.name"
            const val ALIASES = "commands.globalchest.aliases"
            const val DESC = "commands.globalchest.description"
            const val PERM_BASE = "commands.globalchest.permissions.base"
            const val PERM_OTHERS = "commands.globalchest.permissions.others"
        }

        object Do {
            const val NAME = "commands.do.name"
            const val DESC = "commands.do.description"
            const val PERM = "commands.do.permission"
        }

        object Me {
            const val NAME = "commands.me.name"
            const val DESC = "commands.me.description"
            const val PERM = "commands.me.permission"
        }

        object SetHome {
            const val NAME = "commands.sethome.name"
            const val DESC = "commands.sethome.description"
            const val PERM = "commands.sethome.permission"
            const val MSG_SET = "commands.sethome.messages.homeSet"
        }

        object Home {
            const val NAME = "commands.home.name"
            const val DESC = "commands.home.description"
            const val PERM = "commands.home.permission"
            const val MSG_TELEPORTED = "commands.home.messages.teleported"
            const val MSG_NOT_EXIST = "commands.home.messages.homeDoesNotExist"
        }

        object RecInv {
            const val NAME = "commands.recinv.name"
            const val DESC = "commands.recinv.description"
            const val PERM = "commands.recinv.permission"
            const val MSG_RECOVERED = "commands.recinv.messages.inventoryRecovered"
        }

        object Gmc {
            const val NAME = "commands.gmc.name"
            const val DESC = "commands.gmc.description"
            const val PERM_BASE = "commands.gmc.permissions.base"
            const val PERM_OTHERS = "commands.gmc.permissions.others"
            const val USAGE = "commands.gmc.usage"
            const val MSG_SELF = "commands.gmc.messages.self"
            const val MSG_OTHERS = "commands.gmc.messages.others"
        }

        object Gms {
            const val NAME = "commands.gms.name"
            const val DESC = "commands.gms.description"
            const val PERM_BASE = "commands.gms.permissions.base"
            const val PERM_OTHERS = "commands.gms.permissions.others"
            const val USAGE = "commands.gms.usage"
            const val MSG_SELF = "commands.gms.messages.self"
            const val MSG_OTHERS = "commands.gms.messages.others"
        }

        object Gmsp {
            const val NAME = "commands.gmsp.name"
            const val DESC = "commands.gmsp.description"
            const val PERM_BASE = "commands.gmsp.permissions.base"
            const val PERM_OTHERS = "commands.gmsp.permissions.others"
            const val USAGE = "commands.gmsp.usage"
            const val MSG_SELF = "commands.gmsp.messages.self"
            const val MSG_OTHERS = "commands.gmsp.messages.others"
        }

        object Gma {
            const val NAME = "commands.gma.name"
            const val DESC = "commands.gma.description"
            const val PERM_BASE = "commands.gma.permissions.base"
            const val PERM_OTHERS = "commands.gma.permissions.others"
            const val USAGE = "commands.gma.usage"
            const val MSG_SELF = "commands.gma.messages.self"
            const val MSG_OTHERS = "commands.gma.messages.others"
        }

        object Vanish {
            const val NAME = "commands.vanish.name"
            const val DESC = "commands.vanish.description"
            const val PERM = "commands.vanish.permission"
            const val MSG_VANISHED = "commands.vanish.messages.vanished"
            const val MSG_UNVANISHED = "commands.vanish.messages.unvanished"
        }

        object Warp {
            const val NAME = "commands.warp.name"
            const val DESC = "commands.warp.description"
            const val PERM = "commands.warp.permission"
            const val USAGE = "commands.warp.usage"
            const val MSG_NO_WARPS = "commands.warp.messages.noWarpsStored"
            const val MSG_LIST = "commands.warp.messages.warpList"

            object Add {
                const val NAME = "commands.warp.subcommands.add.name"
                const val DESC = "commands.warp.subcommands.add.description"
                const val PERM = "commands.warp.subcommands.add.permission"
                const val USAGE = "commands.warp.subcommands.add.usage"
                const val MSG_ADDED = "commands.warp.subcommands.add.messages.warpAdded"
                const val MSG_EXISTS = "commands.warp.subcommands.add.messages.warpAlreadyExists"
            }

            object Remove {
                const val NAME = "commands.warp.subcommands.remove.name"
                const val DESC = "commands.warp.subcommands.remove.description"
                const val PERM = "commands.warp.subcommands.remove.permission"
                const val USAGE = "commands.warp.subcommands.remove.usage"
                const val MSG_REMOVED = "commands.warp.subcommands.remove.messages.warpRemoved"
                const val MSG_NOT_FOUND = "commands.warp.subcommands.remove.messages.warpNotFound"
            }
        }

        object Spy {
            const val NAME = "commands.spy.name"
            const val DESC = "commands.spy.description"
            const val PERM = "commands.spy.permission"
            const val MSG_ENABLED = "commands.spy.messages.enabled"
            const val MSG_DISABLED = "commands.spy.messages.disabled"
        }

        object Freeze {
            const val NAME = "commands.freeze.name"
            const val DESC = "commands.freeze.description"
            const val PERM = "commands.freeze.permission"
            const val USAGE = "commands.freeze.usage"
            const val MSG_FROZEN = "commands.freeze.messages.frozen"
            const val MSG_UNFROZEN = "commands.freeze.messages.unfrozen"
            const val MSG_BEEN_FROZEN = "commands.freeze.messages.beenFrozen"
            const val MSG_BEEN_UNFROZEN = "commands.freeze.messages.beenUnfrozen"
        }

        object Heal {
            const val NAME = "commands.heal.name"
            const val DESC = "commands.heal.description"
            const val PERM_BASE = "commands.heal.permissions.base"
            const val PERM_OTHERS = "commands.heal.permissions.others"
            const val USAGE = "commands.heal.usage"
            const val MSG_HEALED_SELF = "commands.heal.messages.healedSelf"
            const val MSG_HEALED_PLAYER = "commands.heal.messages.healedPlayer"
            const val MSG_BEEN_HEALED = "commands.heal.messages.beenHealed"
        }

        object Sun {
            const val NAME = "commands.sun.name"
            const val DESC = "commands.sun.description"
            const val PERM = "commands.sun.permission"
            const val MSG_SET = "commands.sun.messages.sunSet"
        }

        object Rain {
            const val NAME = "commands.rain.name"
            const val DESC = "commands.rain.description"
            const val PERM = "commands.rain.permission"
            const val MSG_SET = "commands.rain.messages.rainSet"
        }

        object Thunder {
            const val NAME = "commands.thunder.name"
            const val DESC = "commands.thunder.description"
            const val PERM = "commands.thunder.permission"
            const val MSG_SET = "commands.thunder.messages.thunderSet"
        }

        object Day {
            const val NAME = "commands.day.name"
            const val DESC = "commands.day.description"
            const val PERM = "commands.day.permission"
            const val MSG_SET = "commands.day.messages.daySet"
        }

        object Night {
            const val NAME = "commands.night.name"
            const val DESC = "commands.night.description"
            const val PERM = "commands.night.permission"
            const val MSG_SET = "commands.night.messages.nightSet"
        }

        object Invsee {
            const val NAME = "commands.invsee.name"
            const val DESC = "commands.invsee.description"
            const val PERM = "commands.invsee.permission"
            const val USAGE = "commands.invsee.usage"
            const val MSG_OPENED = "commands.invsee.messages.opened"
        }

        object Back {
            const val NAME = "commands.back.name"
            const val DESC = "commands.back.description"
            const val PERM = "commands.back.permission"
            const val USAGE = "commands.back.usage"
            const val MSG_SUCCESS = "commands.back.messages.success"
        }

        object FlySpeed {
            const val NAME = "commands.flyspeed.name"
            const val ALIASES = "commands.flyspeed.aliases"
            const val DESC = "commands.flyspeed.description"
            const val PERM = "commands.flyspeed.permission"
            const val USAGE = "commands.flyspeed.usage"
            const val MSG_CHANGED = "commands.flyspeed.messages.changed"
            const val MSG_RESET = "commands.flyspeed.messages.reset"
        }

        object Sequence {
            const val NAME = "commands.sequence.name"
            const val ALIASES = "commands.sequence.aliases"
            const val DESC = "commands.sequence.description"
            const val PERM = "commands.sequence.permission"
            const val USAGE = "commands.sequence.usage"
            const val MSG_SUCCESS = "commands.sequence.messages.success"
            const val MSG_ERROR = "commands.sequence.messages.error"
        }

        object Nick {
            const val NAME = "commands.nick.name"
            const val DESC = "commands.nick.description"
            const val PERM_BASE = "commands.nick.permissions.base"
            const val PERM_OTHERS = "commands.nick.permissions.others"
            const val USAGE = "commands.nick.usage"
            const val MSG_SET = "commands.nick.messages.set"
            const val MSG_SET_OTHERS = "commands.nick.messages.setOthers"
            const val MSG_RESET = "commands.nick.messages.reset"
            const val MSG_RESET_OTHERS = "commands.nick.messages.resetOthers"
        }
    }
}