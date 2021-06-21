package net.kunmc.lab.seihekizinrou

import dev.kotx.flylib.*
import io.papermc.paper.event.player.*
import net.kunmc.lab.seihekizinrou.commands.*
import org.bukkit.plugin.java.*

class SeihekiZinrou : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()
        flyLib {
            listen<AsyncChatEvent> {

            }

            command {
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