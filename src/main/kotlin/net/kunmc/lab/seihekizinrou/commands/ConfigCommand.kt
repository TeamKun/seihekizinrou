package net.kunmc.lab.seihekizinrou.commands

import dev.kotx.flylib.command.*
import java.awt.*

object ConfigCommand : Command("config") {
    init {
        usage {
            literalArgument("time_input")
            intArgument("seconds", 1)
        }
        usage {
            literalArgument("time_day")
            intArgument("seconds", 30)
        }
        usage {
            literalArgument("time_night")
            intArgument("seconds", 1)
        }
        usage {
            literalArgument("werewolf_number")
            intArgument("number", 1)
        }
    }

    override fun CommandContext.execute() {
        plugin.reloadConfig()
        when (args.size) {
            1 -> {
                send {
                    append(args[0], Color.GREEN)
                    append(" is now ", Color.LIGHT_GRAY)
                    append(plugin.config.get(args[0]).toString(), Color.GREEN)
                }
            }

            2 -> {
                plugin.config.set(args[0], args[1].toInt())
                plugin.saveConfig()

                send {
                    append("Set ", Color.LIGHT_GRAY)
                    append(args[0], Color.GREEN)
                    append(" to ", Color.LIGHT_GRAY)
                    append(plugin.config.get(args[0]).toString(), Color.GREEN)
                }
            }

            else -> sendHelp()
        }
    }
}