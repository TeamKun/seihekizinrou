package net.kunmc.lab.seihekizinrou.commands

import dev.kotx.flylib.command.*
import dev.kotx.flylib.utils.*
import kotlinx.coroutines.*
import net.kunmc.lab.seihekizinrou.*
import net.kyori.adventure.text.format.*
import java.awt.*
import java.security.*
import java.util.*
import kotlin.concurrent.*

object StartCommand : Command("start") {
    private var waitingTimer = Timer()
    private var waitingCount = 0

    private val scope = CoroutineScope(Dispatchers.Default + CoroutineName("Gateway Client"))

    override fun CommandContext.execute() {
        server!!.title(
            "性癖人狼".asTextComponent(Style.style(TextColor.color(Color.RED.rgb), TextDecoration.BOLD)),
            "自分の性癖をチャットに入力してね！".asTextComponent(),
            3,
            3,
            3
        )
        waitingTimer.cancel()
        waitingTimer = Timer()
        plugin.reloadConfig()
        waitingCount = plugin.config.getInt("time_input")

        waitingTimer.scheduleAtFixedRate(1000, 1000) {
            waitingCount--

            if (waitingCount > 0) {
                val last = server!!.onlinePlayers.size - SeihekiZinrou.propensities.size
                server!!.sendActionBar("${last}人の性癖の入力を待機中... || 開始まで残り${waitingCount}秒".asTextComponent())

                if (last == 0) {
                    cancel()
                    scope.launch { start() }
                }

                return@scheduleAtFixedRate
            }

            plugin.reloadConfig()
            val werewolfNumber = plugin.config.getInt("werewolf_number")

            if (werewolfNumber >= SeihekiZinrou.propensities.size) {
                server!!.sendActionBar("参加人数が設定された人狼の数より少ないためゲームを開始出来ませんでした。".asTextComponent(Color.RED))
            } else {
                scope.launch { start() }
            }

            cancel()
        }
    }

    private suspend fun CommandContext.start() {
        delay(2000)
        val werewolves = SeihekiZinrou.propensities.shuffled(SecureRandom()).subList(0, plugin.config.getInt("werewolf_number"))
        SeihekiZinrou.werewolves = werewolves.map { it.player.uniqueId.toString() }
        werewolves.forEach {
            it.player
        }
    }
}