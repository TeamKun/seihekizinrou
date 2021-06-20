package net.kunmc.lab.seihekizinrou

import dev.kotx.flylib.*
import dev.kotx.flylib.command.*
import dev.kotx.flylib.utils.*
import io.papermc.paper.event.player.*
import org.bukkit.entity.*
import org.bukkit.plugin.java.*
import java.util.*
import java.util.concurrent.*
import kotlin.concurrent.*

class SeihekiZinrou : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()
        flyLib {
            listen<AsyncChatEvent> { event ->
                if (State.isWaitingInput) {
                    State.seihekiList.removeIf { it.player.uniqueId == event.player.uniqueId }
                    State.seihekiList.add(Seiheki(event.player, event.message().content()))
                    if (event.player.server.onlinePlayers.all { serverPlayer -> State.seihekiList.any { it.player.uniqueId == serverPlayer.uniqueId } }) {
                        select(this@SeihekiZinrou)
                    }
                    event.isCancelled = true
                }
            }

            command {
                register("szinrou") {
                    child(StartCommand, EndCommand, SelectCommand, ConfigCommand)
                }
            }
        }
    }
}

private fun select(
    plugin: JavaPlugin
) {
    State.isWaitingInput = false
    State.waitingCount = 0
    State.waitingTimer.cancel()

    plugin.reloadConfig()
    val number = plugin.config.getInt("werewolf_number")

    State.werewolves = State.seihekiList.shuffled().subList(0, number)
}

object StartCommand : Command("start") {
    override fun CommandContext.execute() {
        plugin.reloadConfig()
        val selectTime = plugin.config.getInt("selectTime")

        State.isWaitingInput = true

        State.waitingTimer.cancel()
        State.waitingTimer = Timer()

        State.waitingTimer.scheduleAtFixedRate(0, 1000) {
            State.waitingCount++

            server?.onlinePlayers?.forEach {
                it.sendActionBar("あなたの性癖を入力してください！(時間内であれば何度も再入力できます。) || 残り${selectTime - State.waitingCount}秒".asTextComponent())
            }

            if (State.waitingCount >= selectTime) select(plugin)
        }
    }
}

object EndCommand : Command("end")

object SelectCommand : Command("select") {
    override fun CommandContext.execute() {
        select(plugin)
    }
}

object ConfigCommand : Command("config") {
    override val children = mutableListOf(SelectTimeCommand, WerewolfNumberCommand)
}

object SelectTimeCommand : Command("select_time") {
    init {
        usage {
            intArgument("seconds", 1)

            executes {
                plugin.reloadConfig()
                plugin.config.set("selectTime", args.first().toInt())
                plugin.saveConfig()

                success("Saved!")
            }
        }
    }

    override fun CommandContext.execute() {
        plugin.reloadConfig()
        send(plugin.config.getInt("selectTime").toString())
    }
}

object WerewolfNumberCommand : Command("werewolf_number") {
    init {
        usage {
            intArgument("number", 1)

            executes {
                plugin.reloadConfig()
                plugin.config.set("werewolfNumber", args.first().toInt())
                plugin.saveConfig()

                success("Saved!")
            }
        }
    }

    override fun CommandContext.execute() {
        plugin.reloadConfig()
        send(plugin.config.getInt("werewolfNumber").toString())
    }
}

object State {
    var isWaitingInput = false
    var waitingTimer = Timer()
    var waitingCount = 0
    val seihekiList = mutableListOf<Seiheki>()
    var werewolves: List<Seiheki> = emptyList()
}

data class Seiheki(val player: Player, val seiheki: String)