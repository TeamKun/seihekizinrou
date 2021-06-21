package net.kunmc.lab.seihekizinrou.commands

import dev.kotx.flylib.command.*
import dev.kotx.flylib.utils.*
import net.kunmc.lab.seihekizinrou.*
import net.kyori.adventure.text.format.*
import java.awt.*
import java.util.*
import kotlin.concurrent.*

object StartCommand : Command("start") {
    private var waitingTimer = Timer()
    private var waitingCount = 0

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
            if (waitingCount <= 0) {
                cancel()
            } else {
                val last = server!!.onlinePlayers.size - SeihekiZinrou.propensities.size
                server!!.actionBar("${last}人の性癖の入力を待機中... || 開始まで残り${waitingCount}秒")
            }
        }
    }
}