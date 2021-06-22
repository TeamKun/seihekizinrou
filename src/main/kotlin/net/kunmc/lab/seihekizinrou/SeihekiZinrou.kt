package net.kunmc.lab.seihekizinrou

import dev.kotx.flylib.*
import dev.kotx.flylib.command.internal.*
import dev.kotx.flylib.utils.*
import io.papermc.paper.event.player.*
import net.kunmc.lab.seihekizinrou.commands.*
import org.bukkit.plugin.java.*

class SeihekiZinrou : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()
        flyLib {
            listen<AsyncChatEvent> { event ->
                if (!StartCommand.isWaiting) return@listen

                propensities.removeIf { it.player.uniqueId == event.player.uniqueId }
                propensities.add(Propensity(event.player, event.message().content()))
                event.isCancelled = true
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
    }
}