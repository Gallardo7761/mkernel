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

        object PiglinCartel {
            const val MAIN = "modules.piglinCartel"
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
            const val AGGRO_TIME = "settings.lawEnforcement.aggroTimeSeconds"
            const val SPAWN_DELAY = "settings.lawEnforcement.spawnDelaySeconds"
            const val DESPAWN_TIME = "settings.lawEnforcement.despawnTimeSeconds"
            const val MAX_GOLEMS = "settings.lawEnforcement.maxGolems"
            const val GOLEM_BASE_LEVEL = "settings.lawEnforcement.golemBaseLevel"
            const val GOLEM_LEVEL_MULTIPLIER = "settings.lawEnforcement.golemLevelMultiplier"
        }

        object Paysheet {
            const val TCP_PERCENT = "settings.paysheet.tcpPercent"
        }

        object PiglinCartel {
            const val TAX_AMOUNT = "settings.piglinCartel.taxAmount"
            const val CURRENCY = "settings.piglinCartel.currency"
        }
    }

    object Commands {
        object Arguments {
            const val CATEGORY = "arguments.category"
            const val ARTICLE = "arguments.article"
            const val ARTICLE_OR_ALL = "arguments.articleOrAll"
            const val NAME = "arguments.name"
            const val DESC = "arguments.description"
            const val FINE = "arguments.fine"
            const val STATUS = "arguments.status"
            const val QUANTITY = "arguments.quantity"
        }

        object Articles {
            const val NAME = "commands.articles.name"
            const val ALIASES = "commands.articles.aliases"
            const val DESC = "commands.articles.description"
            const val PERM = "commands.articles.permission"
            const val USAGE = "commands.articles.usage"

            object Add {
                const val NAME = "commands.articles.subcommands.add.name"
                const val DESC = "commands.articles.subcommands.add.description"
                const val PERM = "commands.articles.subcommands.add.permission"
                const val USAGE = "commands.articles.subcommands.add.usage"
            }

            object Remove {
                const val NAME = "commands.articles.subcommands.remove.name"
                const val DESC = "commands.articles.subcommands.remove.description"
                const val PERM = "commands.articles.subcommands.remove.permission"
                const val USAGE = "commands.articles.subcommands.remove.usage"
            }

            object List {
                const val NAME = "commands.articles.subcommands.list.name"
                const val DESC = "commands.articles.subcommands.list.description"
                const val PERM = "commands.articles.subcommands.list.permission"
                const val USAGE = "commands.articles.subcommands.list.usage"
            }

            object Categories {
                const val NAME = "commands.articles.subcommands.categories.name"
                const val ALIASES = "commands.articles.subcommands.categories.aliases"
                const val DESC = "commands.articles.subcommands.categories.description"
                const val PERM = "commands.articles.subcommands.categories.permission"
                const val USAGE = "commands.articles.subcommands.categories.usage"

                object Add {
                    const val NAME = "commands.articles.subcommands.categories.subcommands.add.name"
                    const val DESC = "commands.articles.subcommands.categories.subcommands.add.description"
                    const val PERM = "commands.articles.subcommands.categories.subcommands.add.permission"
                    const val USAGE = "commands.articles.subcommands.categories.subcommands.add.usage"
                }

                object Remove {
                    const val NAME = "commands.articles.subcommands.categories.subcommands.remove.name"
                    const val DESC = "commands.articles.subcommands.categories.subcommands.remove.description"
                    const val PERM = "commands.articles.subcommands.categories.subcommands.remove.permission"
                    const val USAGE = "commands.articles.subcommands.categories.subcommands.remove.usage"
                }

                object List {
                    const val NAME = "commands.articles.subcommands.categories.subcommands.list.name"
                    const val DESC = "commands.articles.subcommands.categories.subcommands.list.description"
                    const val PERM = "commands.articles.subcommands.categories.subcommands.list.permission"
                    const val USAGE = "commands.articles.subcommands.categories.subcommands.list.usage"
                }
            }
        }

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
            }

            object SetStatus {
                const val NAME = "commands.record.subcommands.setstatus.name"
                const val DESC = "commands.record.subcommands.setstatus.description"
                const val PERM = "commands.record.subcommands.setstatus.permission"
                const val USAGE = "commands.record.subcommands.setstatus.usage"
            }
        }

        object Paysheet {
            const val NAME = "commands.paysheet.name"
            const val DESC = "commands.paysheet.description"
            const val PERM = "commands.paysheet.permission"
            const val USAGE = "commands.paysheet.usage"
            const val MSG_HAS_MONEY = "commands.paysheet.messages.hasMoney"

            object Retain {
                const val NAME = "commands.paysheet.subcommands.retain.name"
                const val DESC = "commands.paysheet.subcommands.retain.description"
                const val PERM = "commands.paysheet.subcommands.retain.permission"
                const val USAGE = "commands.paysheet.subcommands.retain.usage"
            }

            object Receive {
                const val NAME = "commands.paysheet.subcommands.receive.name"
                const val DESC = "commands.paysheet.subcommands.receive.description"
                const val PERM = "commands.paysheet.subcommands.receive.permission"
                const val USAGE = "commands.paysheet.subcommands.receive.usage"

                const val MSG_BANCO_ERROR = "commands.paysheet.subcommands.receive.messages.bancoError"
                const val MSG_NO_PAYSHEET = "commands.paysheet.subcommands.receive.messages.noPaysheet"
                const val MSG_SUCCESS = "commands.paysheet.subcommands.receive.messages.success"
                const val MSG_NO_MONEY_IN_SERVER_ACCOUNT = "commands.paysheet.subcommands.receive.messages.noMoneyInServerAccount"
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

            object Categories {
                const val ADD_SUCCESS = "modules.lawEnforcement.categories.addSuccess"
                const val REMOVE_SUCCESS = "modules.lawEnforcement.categories.removeSuccess"
                const val REMOVE_FAIL = "modules.lawEnforcement.categories.removeFail"
                const val NOT_FOUND = "modules.lawEnforcement.categories.notFound"
                const val LIST_HEADER = "modules.lawEnforcement.categories.listHeader"
                const val LIST_ITEM = "modules.lawEnforcement.categories.listItem"
                const val LIST_EMPTY = "modules.lawEnforcement.categories.listEmpty"
            }

            object Articles {
                const val ADD_SUCCESS = "modules.lawEnforcement.articles.addSuccess"
                const val REMOVE_SUCCESS = "modules.lawEnforcement.articles.removeSuccess"
                const val REMOVE_FAIL = "modules.lawEnforcement.articles.removeFail"
                const val NOT_FOUND = "modules.lawEnforcement.articles.notFound"
                const val LIST_HEADER = "modules.lawEnforcement.articles.listHeader"
                const val LIST_ITEM = "modules.lawEnforcement.articles.listItem"
                const val LIST_EMPTY = "modules.lawEnforcement.articles.listEmpty"
            }

            object Record {
                const val ADD_SUCCESS = "modules.lawEnforcement.record.addSuccess"
                const val SET_STATUS_SUCCESS = "modules.lawEnforcement.record.setStatusSuccess"
                const val INVALID_ARGUMENT = "modules.lawEnforcement.record.invalidArgument"
                const val HEADER = "modules.lawEnforcement.record.header"
                const val ITEM_PENDING = "modules.lawEnforcement.record.itemPending"
                const val ITEM_PAID = "modules.lawEnforcement.record.itemPaid"
                const val ITEM_FORGIVEN = "modules.lawEnforcement.record.itemForgiven"
                const val FOOTER = "modules.lawEnforcement.record.footer"
                const val EMPTY = "modules.lawEnforcement.record.empty"
            }

            const val GOLEM_NAME = "modules.lawEnforcement.golemName"
        }

        object Shit {
            object _Meta {
                const val NAME = "modules.shit._meta.name"
                const val ICON = "modules.shit._meta.icon"
            }
        }

        object Paysheet {
            object _Meta {
                const val NAME = "modules.paysheet._meta.name"
                const val ICON = "modules.paysheet._meta.icon"
            }
        }

        object PiglinCartel {
            object _Meta {
                const val NAME = "modules.piglinCartel._meta.name"
                const val ICON = "modules.piglinCartel._meta.icon"
            }

            const val MSG_PAID = "modules.piglinCartel.paid"
            const val MSG_NOT_ENOUGH_TRIBUTE = "modules.piglinCartel.notEnoughTribute"
        }
    }
}
