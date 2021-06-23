package net.kunmc.lab.seihekizinrou

import dev.kotx.flylib.*
import dev.kotx.flylib.command.internal.*
import dev.kotx.flylib.utils.*
import io.papermc.paper.event.player.*
import net.kunmc.lab.seihekizinrou.commands.*
import org.bukkit.event.player.*
import org.bukkit.plugin.java.*

class SeihekiZinrou : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()
        flyLib {
            listen<AsyncChatEvent> { event ->
                if (step == Step.WAITING_INPUT) {
                    propensities.removeIf { it.player.uniqueId == event.player.uniqueId }
                    propensities.add(Propensity(event.player, event.message().content()))
                    event.isCancelled = true
                    return@listen
                }

                if (propensities.find { it.player.uniqueId == event.player.uniqueId }?.dead == true) {
                    event.isCancelled = true
                    return@listen
                }
            }

            listen<PlayerMoveEvent> { event ->
                if (step == Step.WAITING_KILL) event.isCancelled = true
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
        WAITING_KILL,
        FINISHED
    }
}
