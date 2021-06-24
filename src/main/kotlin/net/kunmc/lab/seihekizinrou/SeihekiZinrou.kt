package net.kunmc.lab.seihekizinrou

import dev.kotx.flylib.*
import dev.kotx.flylib.command.internal.*
import dev.kotx.flylib.menu.menus.*
import dev.kotx.flylib.utils.*
import io.papermc.paper.event.player.*
import net.kunmc.lab.seihekizinrou.commands.*
import org.bukkit.entity.*
import org.bukkit.event.inventory.*
import org.bukkit.event.player.*
import org.bukkit.plugin.java.*
import java.awt.*

class SeihekiZinrou : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()
        flyLib {
            listen<AsyncChatEvent> { event ->
                if (step == Step.WAITING_INPUT) {
                    propensities.removeIf { it.player.uniqueId == event.player.uniqueId }
                    propensities.add(Propensity(event.player, event.message().content()))
                    event.player.send {
                        append("性癖「", Color.GREEN)
                        bold(event.message().content())
                        append("」を記録しました。", Color.GREEN)
                    }
                    event.isCancelled = true
                    return@listen
                }

                if (propensities.find { it.player.uniqueId == event.player.uniqueId }?.dead == true) {
                    event.isCancelled = true
                    return@listen
                }
            }

            listen<InventoryCloseEvent> { event ->
                if(step == Step.NIGHT && propensities.find { it.player.uniqueId == event.player.uniqueId }?.werewolf == true) {
                    ChestMenu.display(event.player as Player) {

                    }
                }
            }

            listen<PlayerMoveEvent> { event ->
                if (step == Step.NIGHT) event.isCancelled = true
            }

            command {
                defaultConfiguration {
                    permission(Permission.OP)
                }

                register("szinrou") {
                    child(StartCommand, EndCommand, ConfigCommand)
                }
            }
        }
    }

    companion object {
        val propensities = mutableListOf<Propensity>()
        var step = Step.NOT_STARTED
        var day: Int = 1
    }

    enum class Step {
        NOT_STARTED,
        WAITING_INPUT,
        ROLE_ANNOUNCEMENT,
        MORNING,
        DAY,
        SUNSET,
        NIGHT,
        FINISHED
    }
}
