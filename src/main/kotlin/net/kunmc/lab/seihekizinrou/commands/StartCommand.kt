package net.kunmc.lab.seihekizinrou.commands

import dev.kotx.flylib.command.*
import dev.kotx.flylib.menu.menus.*
import dev.kotx.flylib.utils.*
import kotlinx.coroutines.*
import net.kunmc.lab.seihekizinrou.*
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.format.*
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.inventory.meta.*
import java.awt.Color
import java.util.*
import kotlin.concurrent.*

object StartCommand : Command("start") {
    internal var timer = Timer()
    internal var count = 0
    var isWaiting = false

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun CommandContext.execute() {
        server!!.playSound(
            Sound.sound(
                org.bukkit.Sound.AMBIENT_CAVE.key,
                Sound.Source.AMBIENT,
                1f,
                1f
            )
        )
        server!!.title(
            "性癖人狼".component(Color.RED, TextDecoration.BOLD),
            "自分の性癖をチャットに入力してください".component(),
            1,
            7,
            1
        )
        isWaiting = true
        timer.cancel()
        timer = Timer()
        SeihekiZinrou.propensities.clear()
        plugin.reloadConfig()
        count = plugin.config.getInt("time_input")

        runBlocking { delay(2000) }
        timer.scheduleAtFixedRate(1000, 1000) {
            count--

            if (count > 0) {
                val last = server!!.onlinePlayers.size - SeihekiZinrou.propensities.size
                server!!.sendActionBar("自分の性癖をチャットに入力してください || ${last}人の入力を待機中... || 残り${count}秒".component())

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
        isWaiting = false

        plugin.reloadConfig()
        val werewolfNumber = plugin.config.getInt("werewolf_number")

//        if (werewolfNumber >= SeihekiZinrou.propensities.size) {
//            server!!.sendActionBar("参加人数が設定された人狼の数より少ないためゲームを開始出来ませんでした。".asTextComponent(Color.RED))
//            return
//        }

        delay(2000)

        title(
            "間もなく役職が発表されます...".component(),
            "".component(),
            1,
            3,
            1
        )

        delay(7000)
        SeihekiZinrou.propensities.shuffle()
        SeihekiZinrou.propensities.subList(0, werewolfNumber).forEach { it.werewolf = true }

        SeihekiZinrou.propensities.forEach {
            it.player.title(
                "役職発表".component(),
                text {
                    append("あなたは")
                    if (it.werewolf)
                        bold("人狼", Color.RED)
                    else
                        bold("村人", Color.GREEN)
                    append("です。")

                    append(if (it.werewolf) "自分の性癖がバレないように立ち回り、村人を襲ってください。" else "公表された性癖を持つ人狼を推測し、なるべく早く処刑してください。")
                },
                1,
                7,
                1
            )
        }

        delay(10000)

        val book = item(Material.WRITTEN_BOOK) {
            meta {
                this as BookMeta
                title("人狼の性癖".component())
                author("ゲームマスター".component())
                page {
                    SeihekiZinrou.propensities.filter { it.werewolf }.forEach {
                        append("・", Color.GRAY).append(it.propensity, Color.ORANGE).appendln()
                    }
                }
            }
        }

        SeihekiZinrou.propensities.forEach {
            it.player.inventory.addItem(book)
        }

        title(
            "${werewolfNumber}人の人狼の性癖が公表されました。".component(),
            "配られた本に書かれてある性癖を元に人狼を推測し、処刑対象を話し合ってください。".component(),
            1,
            6,
            1
        )

        delay(7000)

        dayTime()
    }

    fun CommandContext.dayTime() {
        world!!.animateTime(plugin, 1000)
        timer.cancel()
        timer = Timer()
        plugin.reloadConfig()
        count = plugin.config.getInt("time_day")

        val selector = item(Material.STICK) {
            displayName("右クリックして処刑者を選択")
        }

        timer.scheduleAtFixedRate(1000, 1000) {
            count--
            if (count <= 0) {
                SeihekiZinrou.propensities.forEach {
                    it.player.inventory.remove(selector)
                    selector.unRegister()
                }
                scope.launch { punishment() }
                cancel()
                return@scheduleAtFixedRate
            }

            if (count == 30) {
                title("残り30秒".component(), "処刑者を選択するアイテムが与えられました。".component(), 1, 3, 1)

                val menu = ChestMenu.menu {
                    SeihekiZinrou.propensities.forEachIndexed { i, propensity ->
                        item(i, item(Material.PLAYER_HEAD) {
                            displayName(propensity.player.name)
                            meta {
                                this as SkullMeta
                                owningPlayer = propensity.player
                            }
                        }) { event ->
                            SeihekiZinrou.propensities.forEach { it.votes.removeIf { it.uniqueId == event.whoClicked.uniqueId } }
                            propensity.votes.add(event.whoClicked as Player)
                            event.whoClicked.closeInventory()
                        }
                    }
                }

                server!!.onlinePlayers.forEach {
                    selector.onClick(it) { event ->
                        menu.display(event.player)
                        false
                    }

                    it.inventory.addItem(selector)
                }
            }

            if (count == 15) title("残り15秒".component(), "".component(), 1, 3, 1)

            actionbar("人狼を推測し、誰を処刑するかを話し合ってください。 || 残り${count}秒".component())
        }
    }

    suspend fun CommandContext.punishment() {
        val target = SeihekiZinrou.propensities.maxByOrNull { it.votes.size }!!
        plugin.runSync {
                target.player.gameMode = GameMode.SPECTATOR
        }

        target.player.title(
            "あなたは処刑されました。".component(),
            "ゲームが終了するまではチャットは出来ません。".component(),
            1,
            5,
            1
        )

        SeihekiZinrou.propensities.filter { !it.dead }.forEach {
            it.player.title(
                "${target.player.name}が処刑されました。".component(),
                "死んだプレイヤーはゲームが終了するまでチャットをすることが出来ません。".component(),
                1,
                5,
                1
            )
        }
        delay(7000)
        world!!.animateTime(plugin, 12800, 120)
        title("間もなく夜が来ます...".component(), "".component(), 1, 5, 1)
        delay(7000)

        nightTime()
    }

    suspend fun CommandContext.nightTime() {
        world!!.animateTime(plugin, 18000, 120)
        title("夜が来ました。".component(), "人狼は誰を殺害するかを決めてください。".component(), 1, 5, 1)
    }
}