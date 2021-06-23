package net.kunmc.lab.seihekizinrou.commands

import dev.kotx.flylib.command.*
import dev.kotx.flylib.utils.*
import net.kunmc.lab.seihekizinrou.*
import java.util.*

object EndCommand: Command("end") {
    override fun CommandContext.execute() {
        StartCommand.timer.cancel()
        StartCommand.timer = Timer()
        SeihekiZinrou.propensities.clear()
        StartCommand.count = 0

        send("すべての設定をリセットし、性癖人狼を終了しました。")
        server!!.title("ゲームが管理者によって終了させられました。".component(), "".component(), 1, 5, 1)
    }
}