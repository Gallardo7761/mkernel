package net.miarma.mkernel.miarmacraft.common.config

object ConfigKeys {
    object Modules {
        object BlindGod {
            const val MAIN = "modules.blindGod"
        }

        object LawEnforcement {
            const val MAIN = "modules.lawEnforcement"
        }

        object Dictatorship {
            const val MAIN = "modules.dictatorship"
            const val TITHE = "modules.dictatorship.tithe"
            const val FINES = "modules.dictatorship.fines"
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
        }

        object Dictatorship {
            const val TITHE_INTERVAL = "settings.tithe.interval"
            const val TITHE_AMOUNT = "settings.tithe.amount"
            const val FINES_AUTHOR = "settings.fines.author"
            const val FINES_EXPIRATION = "settings.fines.expirationTime"
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

            const val WARNING = "modules.lawEnforcement.warning"
            const val GOLEM_NAME = "modules.lawEnforcement.golemName"
        }

        object Dictatorship {
            object _Meta {
                const val NAME = "modules.dictatorship._meta.name"
                const val ICON = "modules.dictatorship._meta.icon"
            }
        }
    }
}