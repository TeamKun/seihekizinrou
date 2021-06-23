package net.kunmc.lab.seihekizinrou.commands

import dev.kotx.flylib.command.*
import dev.kotx.flylib.menu.menus.*
import dev.kotx.flylib.utils.*
import kotlinx.coroutines.*
import net.kunmc.lab.seihekizinrou.*
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.format.*
import org.bukkit.*
import org.bukkit.enchantments.*
import org.bukkit.entity.*
import org.bukkit.inventory.*
import org.bukkit.inventory.meta.*
import org.bukkit.potion.*
import java.awt.Color
import java.util.*
import kotlin.concurrent.*

object StartCommand : Command("start") {
    internal var timer = Timer()
    internal var count = 0

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun CommandContext.execute() {
        server!!.title(
            "性癖人狼".component(Color.RED, TextDecoration.BOLD),
            "自分の性癖をチャットに入力してください".component(),
            1,
            7,
            1
        )
        SeihekiZinrou.step = SeihekiZinrou.Step.WAITING_INPUT
        timer.cancel()
        timer = Timer()
        SeihekiZinrou.propensities.clear()
        plugin.reloadConfig()
        count = plugin.config.getInt("time_input")

        server!!.playSound(
            Sound.sound(
                org.bukkit.Sound.AMBIENT_CAVE.key,
                Sound.Source.MASTER,
                2f,
                .3f
            )
        )
        world!!.animateTime(plugin, 22000, 100)

        runBlocking { delay(2000) }
        timer.scheduleAtFixedRate(1000, 1000) {
            count--

            if (count > 0) {
                val last = server!!.onlinePlayers.size - SeihekiZinrou.propensities.size
                server!!.sendActionBar(text {
                    +"自分の性癖をチャットに入力してください || ${last}人の入力を待機中... || "
                    append(
                        "残り${count}秒", when (count) {
                            in 0..10 -> Color.RED
                            in 10..30 -> Color.ORANGE
                            else -> Color.GREEN
                        }
                    )
                })

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
        val werewolfNumber = plugin.config.getInt("werewolf_number")
        SeihekiZinrou.step = SeihekiZinrou.Step.ROLE_ANNOUNCEMENT

//        if (werewolfNumber >= SeihekiZinrou.propensities.size) {
//            server!!.sendActionBar("参加人数が設定された人狼の数より少ないためゲームを開始出来ませんでした。".asTextComponent(Color.RED))
//            return
//        }

        delay(2000)

        title(
            "まもなく役職が発表されます...".component(),
            "".component(),
            1,
            3,
            1
        )

        delay(7000)
        SeihekiZinrou.propensities.shuffle()
        SeihekiZinrou.propensities.subList(0, werewolfNumber).forEach { it.werewolf = true }

        server!!.playSound(
            Sound.sound(
                org.bukkit.Sound.AMBIENT_CAVE.key,
                Sound.Source.MASTER,
                2f,
                .3f
            )
        )

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

        delay(5000)

        val wolves = SeihekiZinrou.propensities.filter { it.werewolf }
        if (wolves.size > 1) wolves.forEach { wolf ->
            wolf.player.send {
                bold("--- ", Color.GRAY)
                bold("自分以外の人狼", Color.RED)
                bold(" ---", Color.GRAY)
                wolves.filterNot { it.player.uniqueId == wolf.player.uniqueId }.forEach {
                    bold(it.player.name, Color.GREEN)
                    appendln(":", Color.GRAY)
                    append("  >", Color.GREEN)
                    appendln(" ${it.propensity}")
                    appendln()
                }
            }
        }

        delay(5000)

        morning(true)
    }

    suspend fun CommandContext.morning(
        isFirstTime: Boolean = false,
    ) {
        SeihekiZinrou.step = SeihekiZinrou.Step.MORNING
        world!!.animateTime(plugin, 0)

        server!!.playSound(
            Sound.sound(
                org.bukkit.Sound.ENTITY_PLAYER_LEVELUP.key,
                Sound.Source.MASTER,
                1f,
                1f
            )
        )

        val deadPlayers = SeihekiZinrou.propensities.filter { it.killed }
        if (deadPlayers.isEmpty()) {
            title(
                "${SeihekiZinrou.day}日目の朝が来ました。".component(),
                "昨晩は誰も襲われませんでした。".component(),
                1, 3, 1
            )
            delay(5000)
        } else {
            plugin.runSync {
                deadPlayers.forEach {
                    it.player.gameMode = GameMode.SPECTATOR

                    it.player.title(
                        "昨晩、あなたは人狼に襲われました。".component(),
                        "あなたの性癖は参加者全員に公表されます。".component(),
                        1, 3, 1
                    )
                }

                server!!.broadcast(text {
                    bold("--- ", Color.GRAY)
                    bold("襲われた村人とその性癖", Color.RED)
                    bold(" ---", Color.GRAY)
                    deadPlayers.forEach {
                        bold(it.player.name, Color.GREEN)
                        appendln(":", Color.GRAY)
                        append("  >", Color.GREEN)
                        appendln(" ${it.propensity}")
                        appendln()
                    }
                })

                SeihekiZinrou.propensities.filterNot { it.killed }.forEach {
                    it.player.title(
                        "${SeihekiZinrou.day}日目の朝が来ました。".component(),
                        "昨晩${deadPlayers}人の村人が襲われました。".component(),
                        1, 5, 1
                    )
                }

                deadPlayers.toList().forEach { p ->
                    val target = SeihekiZinrou.propensities.find { it.player.uniqueId == p.player.uniqueId }
                    target?.killed = false
                    target?.dead = false
                }
            }
            delay(7000)
        }

        if (isFirstTime) {
            val werewolfNumber = plugin.config.getInt("werewolf_number")
            val book = item(Material.WRITTEN_BOOK) {
                meta {
                    this as BookMeta
                    title("人狼の性癖".component())
                    author("ゲームマスター".component())
                    page {
                        boldln("----------------", Color.GRAY)
                        appendln("人狼は${werewolfNumber}匹存在します。(ゲーム開始時の匹数)")
                        boldln("----------------", Color.GRAY)
                        appendln()
                        SeihekiZinrou.propensities.filter { it.werewolf }.forEach {
                            bold("・", Color.GRAY).append(it.propensity, Color.ORANGE).appendln()
                        }
                    }
                }
            }

            SeihekiZinrou.propensities.forEach {
                it.player.inventory.addItem(book)
            }

            title(
                "人狼の性癖が公表されました。".component(),
                "配られた本に書かれてある性癖を元に人狼を推測し、処刑対象を話し合ってください。".component(),
                1,
                6,
                1
            )
            delay(7000)
        } else {
            title(
                "もうすぐ昼になります。".component(),
                "配られた本や、殺害された村人の性癖から人狼を推測し、処刑対象を話し合ってください。".component(),
                1,
                6,
                1
            )
            delay(7000)
        }


        dayTime()
    }

    private fun CommandContext.dayTime() {
        SeihekiZinrou.step = SeihekiZinrou.Step.DAY
        world!!.animateTime(plugin, 4000)
        timer.cancel()
        timer = Timer()
        plugin.reloadConfig()
        count = plugin.config.getInt("time_day")

        val selector = item(Material.CLOCK) {
            enchant(Enchantment.LUCK)
            flag(ItemFlag.HIDE_ENCHANTS)
            displayName("右クリックして処刑者を選択")
        }

        timer.scheduleAtFixedRate(1000, 1000) {
            count--
            if (count <= 0) {
                SeihekiZinrou.propensities.forEach {
                    it.player.inventory.remove(selector)
                }
                selector.unRegister()
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
                            event.whoClicked.send {
                                bold(propensity.player.name, Color.GREEN)
                                append("を処刑者として選択しました。")
                            }
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

            if (count == 15) {
                world!!.animateTime(plugin, 12800, 200)
            }

            SeihekiZinrou.propensities.forEach {
                it.player.sendActionBar(text {
                    append("あなたは")
                    if (it.werewolf)
                        bold("人狼", Color.RED).append("です。").append("人狼であるとバレないよう立ち回り、誰を処刑するか話し合ってください。")
                    else
                        bold("村人", Color.GREEN).append("です。").append("人狼を推測し、誰を処刑するかを話し合ってください。")

                    when {
                        count in 10..30 -> append(" || ").append("残り${count}秒", Color.ORANGE)
                        count < 10 -> append(" || ").append("残り${count}秒", Color.RED)
                        else -> append(" || ").append("残り${count}秒", Color.GREEN)
                    }
                })
            }
        }
    }

    private suspend fun CommandContext.punishment() {
        SeihekiZinrou.step = SeihekiZinrou.Step.SUNSET
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
                "誰が誰に投票したかはチャット欄で確認できます。".component(),
                1,
                5,
                1
            )

            it.player.send {
                append("-----------", Color.GRAY).bold("投票結果", Color.GREEN).appendln("-----------", Color.GRAY)
                SeihekiZinrou.propensities.forEach {
                    bold(it.player.name, Color.GREEN)
                    appendln(":", Color.GRAY)

                    it.votes.forEach {
                        append("  > ").appendln(it.name, Color.GREEN)
                    }
                    appendln()
                }
            }
        }

        delay(7000)

        nightTime()
    }

    private suspend fun CommandContext.nightTime() {
        SeihekiZinrou.step = SeihekiZinrou.Step.NIGHT
        world!!.animateTime(plugin, 14000)

        plugin.reloadConfig()
        val nightTime = plugin.config.getInt("time_night")

        SeihekiZinrou.propensities.forEach {
            plugin.runSync {
                it.player.addPotionEffects(
                    listOf(
                        PotionEffect(
                            PotionEffectType.INVISIBILITY,
                            nightTime * 20,
                            1,
                            false,
                            false,
                            false
                        ),
                        PotionEffect(
                            PotionEffectType.BLINDNESS,
                            nightTime * 20,
                            2,
                            false,
                            false,
                            false
                        )
                    )
                )
            }

            if (it.werewolf) {
                title("夜が来ました。".component(), "誰を殺害するかを決めてください。".component(), 1, 5, 1)
                ChestMenu.display(it.player) {

                }
            } else {
                title("夜が来ました。".component(), "人狼の行動が終わるまで暫くお待ちください。".component(), 1, 5, 1)
            }
        }

        delay(nightTime.toLong() * 1000)
    }
}