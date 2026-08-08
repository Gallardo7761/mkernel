package net.miarma.mkernel.miarmacraft.common.config

object ConfigKeys {
    object Modules {
        object BlindGod {
            const val MAIN = "modules.blindGod"
        }

        object LawEnforcement {
            const val MAIN = "modules.lawEnforcement"
        }

        object Shit {
            const val MAIN = "modules.shit"
        }
    }

    object Settings {
        object BlindGod {
            const val Y_THRESHOLD = "settings.blindGod.yThreshold"
            const val FIRST_VALUE = "settings.blindGod.first"
            const val SECOND_VALUE = "settings.blindGod.second"
            const val THIRD_VALUE = "settings.blindGod.third"
        }

        object LawEnforcement {
            const val REGION = "settings.lawEnforcement.region"
            const val IMMUNITY_PERM = "settings.lawEnforcement.immunityPermission"
            const val AGGRO_TIME = "settings.lawEnforcement.aggroTimeSeconds"
            const val SPAWN_DELAY = "settings.lawEnforcement.spawnDelaySeconds"
            const val DESPAWN_TIME = "settings.lawEnforcement.despawnTimeSeconds"
            const val MAX_GOLEMS = "settings.lawEnforcement.maxGolems"
            const val GOLEM_BASE_LEVEL = "settings.lawEnforcement.golemBaseLevel"
            const val GOLEM_LEVEL_MULTIPLIER = "settings.lawEnforcement.golemLevelMultiplier"
        }
    }

    object Commands {
        object Record {
            const val NAME = "commands.record.name"
            const val ALIASES = "commands.record.aliases"
            const val DESC = "commands.record.description"
            const val PERM = "commands.record.permission"
            const val USAGE = "commands.record.usage"

            object Add {
                const val NAME = "commands.record.subcommands.add.name"
                const val DESC = "commands.record.subcommands.add.description"
                const val PERM = "commands.record.subcommands.add.permission"
                const val USAGE = "commands.record.subcommands.add.usage"
                const val MSG_SUCCESS = "commands.record.subcommands.add.messages.success"
            }

            object Clear {
                const val NAME = "commands.record.subcommands.clear.name"
                const val DESC = "commands.record.subcommands.clear.description"
                const val PERM = "commands.record.subcommands.clear.permission"
                const val USAGE = "commands.record.subcommands.clear.usage"
                const val MSG_SUCCESS = "commands.record.subcommands.clear.messages.success"
                const val MSG_NOT_FOUND = "commands.record.subcommands.clear.messages.notFound"
            }

            object ClearAll {
                const val NAME = "commands.record.subcommands.clearall.name"
                const val DESC = "commands.record.subcommands.clearall.description"
                const val PERM = "commands.record.subcommands.clearall.permission"
                const val USAGE = "commands.record.subcommands.clearall.usage"
                const val MSG_SUCCESS = "commands.record.subcommands.clearall.messages.success"
            }

            object Pending {
                const val NAME = "commands.record.subcommands.pending.name"
                const val DESC = "commands.record.subcommands.pending.description"
                const val PERM = "commands.record.subcommands.pending.permission"
                const val USAGE = "commands.record.subcommands.pending.usage"
                const val MSG_SUCCESS = "commands.record.subcommands.pending.messages.success"
                const val MSG_NOT_FOUND = "commands.record.subcommands.pending.messages.notFound"
            }
        }

        object SixSeven {
            const val NAME = "commands.sixseven.name"
            const val DESC = "commands.sixseven.description"
            const val PERM = "commands.sixseven.permission"
            const val USAGE = "commands.sixseven.usage"
            const val MSG = "commands.sixseven.message"
        }
    }

    object Messages {
        object BlindGod {
            object _Meta {
                const val NAME = "modules.blindGod._meta.name"
                const val ICON = "modules.blindGod._meta.icon"
            }

            const val FIRST = "modules.blindGod.first"
            const val SECOND = "modules.blindGod.second"
            const val THIRD = "modules.blindGod.third"
        }

        object LawEnforcement {
            object _Meta {
                const val NAME = "modules.lawEnforcement._meta.name"
                const val ICON = "modules.lawEnforcement._meta.icon"
            }

            object Record {
                const val HEADER = "modules.lawEnforcement.record.header"
                const val ITEM_PENDING = "modules.lawEnforcement.record.itemPending"
                const val ITEM_CLEARED = "modules.lawEnforcement.record.itemCleared"
                const val FOOTER = "modules.lawEnforcement.record.footer"
                const val EMPTY = "modules.lawEnforcement.record.empty"
            }

            const val WARNING = "modules.lawEnforcement.warning"
            const val GOLEM_NAME = "modules.lawEnforcement.golemName"
        }
    }
}