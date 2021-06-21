package net.kunmc.lab.seihekizinrou.commands

import dev.kotx.flylib.command.*
import dev.kotx.flylib.utils.*
import kotlinx.coroutines.*
import net.kunmc.lab.seihekizinrou.*
import net.kyori.adventure.text.format.*
import java.awt.*
import java.util.*
import kotlin.concurrent.*

object StartCommand : Command("start") {
    private var waitingTimer = Timer()
    private var waitingCount = 0
    var isWaiting = false

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun CommandContext.execute() {
        runBlocking {
            server!!.title(
                "まもなく開始されます...".asTextComponent(),
                "".asTextComponent(),
                1,
                3,
                1
            )
            delay(5000)
        }
        server!!.title(
            "性癖人狼".asTextComponent(Style.style(TextColor.color(Color.RED.rgb), TextDecoration.BOLD)),
            "自分の性癖をチャットに入力してね！".asTextComponent(),
            1,
            3,
            1
        )
        isWaiting = true
        waitingTimer.cancel()
        waitingTimer = Timer()
        SeihekiZinrou.propensities.clear()
        plugin.reloadConfig()
        waitingCount = plugin.config.getInt("time_input")

        waitingTimer.scheduleAtFixedRate(1000, 1000) {
            waitingCount--

            if (waitingCount > 0) {
                val last = server!!.onlinePlayers.size - SeihekiZinrou.propensities.size
                server!!.sendActionBar("${last}人の性癖の入力を待機中... || 開始まで残り${waitingCount}秒".asTextComponent())

                if (last == 0) {
                    scope.launch { start() }
                    cancel()
                }

                return@scheduleAtFixedRate
            }

            scope.launch { start() }
            cancel()
        }
    }

    private suspend fun CommandContext.start() {
        plugin.reloadConfig()
        isWaiting = false

        val werewolfNumber = plugin.config.getInt("werewolf_number")

        if (werewolfNumber >= SeihekiZinrou.propensities.size) {
            server!!.sendActionBar("参加人数が設定された人狼の数より少ないためゲームを開始出来ませんでした。".asTextComponent(Color.RED))
            return
        }

        delay(2000)

        server!!.title(
            "あなたの役職が発表されます...".asTextComponent(),
            "".asTextComponent(),
            1,
            3,
            1
        )

        delay(5000)
        SeihekiZinrou.propensities.shuffle()
        SeihekiZinrou.propensities.subList(0, werewolfNumber).forEach { it.werewolf = true }

        SeihekiZinrou.propensities.forEach {
            it.player.title(
                "役職発表".asTextComponent(),
                text {
                    append("あなたは")
                    if (it.werewolf)
                        append("人狼", Style.style(TextColor.color(Color.RED.rgb), TextDecoration.BOLD))
                    else
                        append("村人", Style.style(TextColor.color(Color.GREEN.rgb), TextDecoration.BOLD))
                    append("です。")
                },
                3,
                3,
                3
            )
        }
    }
}