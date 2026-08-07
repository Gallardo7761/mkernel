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
        }
    }

    object Commands {
        object Antecedentes {
            const val NAME = "commands.antecedentes.name"
            const val ALIASES = "commands.antecedentes.aliases"
            const val DESC = "commands.antecedentes.description"
            const val PERM = "commands.antecedentes.permission"
            const val USAGE = "commands.antecedentes.usage"
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

            object Antecedentes {
                const val HEADER = "modules.lawEnforcement.antecedentes.header"
                const val ITEM = "modules.lawEnforcement.antecedentes.item"
                const val FOOTER = "modules.lawEnforcement.antecedentes.footer"
                const val EMPTY = "modules.lawEnforcement.antecedentes.empty"
            }

            const val WARNING = "modules.lawEnforcement.warning"
            const val GOLEM_NAME = "modules.lawEnforcement.golemName"
        }
    }
}