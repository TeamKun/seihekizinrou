package net.kunmc.lab.seihekizinrou.commands

import dev.kotx.flylib.command.*
import net.kunmc.lab.seihekizinrou.*
import java.util.*
import kotlin.concurrent.*

object StartCommand : Command("start") {
    private var waitingTimer = Timer()
    override fun CommandContext.execute() {
        server!!.showTitle("性癖人狼", "自分の性癖をチャットに入力してね！", 3, 3, 3)
        waitingTimer.cancel()
        waitingTimer = Timer()

        waitingTimer.scheduleAtFixedRate(1000, 1000) {

        }
    }
}