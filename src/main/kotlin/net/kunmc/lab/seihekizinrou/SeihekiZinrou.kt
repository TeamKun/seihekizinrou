package net.kunmc.lab.seihekizinrou

import dev.kotx.flylib.*
import dev.kotx.flylib.command.*
import dev.kotx.flylib.utils.*
import io.papermc.paper.event.player.*
import org.bukkit.boss.*
import org.bukkit.entity.*
import org.bukkit.plugin.java.*
import java.util.*
import kotlin.concurrent.*

class SeihekiZinrou : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()
        flyLib {
            listen<AsyncChatEvent> { event ->
                if (State.isWaitingInput) {
                    State.seihekiList.removeIf { it.player.uniqueId == event.player.uniqueId }
                    State.seihekiList.add(Seiheki(event.player, event.message().content()))
                    event.isCancelled = true
                    if (event.player.server.onlinePlayers.all { serverPlayer -> State.seihekiList.any { it.player.uniqueId == serverPlayer.uniqueId } }) {
                        State.isWaitingInput = false
                    }
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

object StartCommand : Command("start") {
    override fun CommandContext.execute() {

        server!!.onlinePlayers.forEach {
            it.send("自身の性癖を入力してください！(時間内であれば何度でも入力し直せます。)")
        }

        plugin.reloadConfig()
        val selectTime = plugin.config.getInt("selectTime")

        State.isWaitingInput = true

        State.waitingTimer.cancel()
        State.waitingTimer = Timer()

        State.waitingBar = server!!.createBossBar(
            "残り時間",
            BarColor.BLUE,
            BarStyle.SEGMENTED_10,
            BarFlag.CREATE_FOG
        ).apply {
            server!!.onlinePlayers.forEach {
                addPlayer(it)
            }
        }

        State.waitingTimer.scheduleAtFixedRate(0, 1000) {
            State.waitingCount++

            State.waitingBar?.progress = State.waitingCount.toDouble() / selectTime

            if (State.waitingCount >= selectTime) {
                State.isWaitingInput = false
                State.waitingBar?.isVisible = false
                State.waitingBar = null
                State.waitingCount = 0
                cancel()
            }
        }
    }
}

object EndCommand : Command("end")

object SelectCommand : Command("select")

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
    var waitingBar: BossBar? = null
    val seihekiList = mutableListOf<Seiheki>()
}

data class Seiheki(val player: Player, val seiheki: String)