package net.kunmc.lab.seihekizinrou

import dev.kotx.flylib.*
import dev.kotx.flylib.command.*
import dev.kotx.flylib.command.Command
import dev.kotx.flylib.command.internal.*
import dev.kotx.flylib.utils.*
import io.papermc.paper.event.player.*
import net.kyori.adventure.text.format.*
import org.bukkit.command.*
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
                    event.player.success("あなたの性癖を${event.message().content()}に設定しました！")
                    if (event.player.server.onlinePlayers.all { serverPlayer -> State.seihekiList.any { it.player.uniqueId == serverPlayer.uniqueId } }) {
                        select(this@SeihekiZinrou)
                    }
                    event.isCancelled = true
                }
            }

            command {
                defaultConfiguration {
                    permission(Permission.OP)
                }
                register("szinrou") {
                    child(StartCommand, StartCommand, EndCommand, SelectCommand, ConfigCommand)
                }
            }
        }
    }
}

private fun select(
    plugin: JavaPlugin,
    sender: CommandSender? = null
) {
    State.isWaitingInput = false
    State.waitingCount = 0
    State.waitingTimer.cancel()

    plugin.reloadConfig()
    val number = plugin.config.getInt("werewolfNumber")

    if (State.seihekiList.size <= number) {
        sender?.fail("人狼の数($number)が参加プレイヤー数(${State.seihekiList.size})以上の為、ゲームを開始することができません。")
        return
    }

    State.werewolves = State.seihekiList.shuffled().subList(0, number)
    State.werewolves.forEach {

    }
}

object StartCommand : Command("start") {
    override fun CommandContext.execute() {
        plugin.reloadConfig()
        val selectTime = plugin.config.getInt("selectTime")

        server?.onlinePlayers?.forEach {
            it.sendTitle("性癖人狼", "あなたの性癖をチャットに入力してね", 20, 80, 20)
        }

        State.waitingCount = 0

        State.isWaitingInput = true

        State.waitingTimer.cancel()
        State.waitingTimer = Timer()

        State.waitingTimer.scheduleAtFixedRate(0, 1000) {
            State.waitingCount++

            server?.onlinePlayers?.forEach {
                it.sendActionBar("あなたの性癖を入力してください！ (時間内であれば何度も再入力できます。) || 残り${selectTime - State.waitingCount}秒".asTextComponent(TextDecoration.BOLD))
            }

            if (State.waitingCount >= selectTime) select(plugin)
        }
    }
}

object EndCommand : Command("end")

object SelectCommand : Command("select") {
    override fun CommandContext.execute() {
        select(plugin, sender)
    }
}

object ConfigCommand : Command("config") {
    override val children = mutableListOf(SelectTimeCommand, WerewolfNumberCommand, ThinkingTimeCommand)
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

object ThinkingTimeCommand : Command("thinking_time") {
    init {
        usage {
            intArgument("seconds", 1)

            executes {
                plugin.reloadConfig()
                plugin.config.set("thinkingTime", args.first().toInt())
                plugin.saveConfig()

                success("Saved!")
            }
        }
    }

    override fun CommandContext.execute() {
        plugin.reloadConfig()
        send(plugin.config.getInt("thinkingTime").toString())
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