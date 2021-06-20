package net.kunmc.lab.seihekizinrou

import dev.kotx.flylib.*
import io.papermc.paper.event.player.*
import org.bukkit.plugin.java.*

class SeihekiZinrou : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()
        flyLib {
            listen<AsyncChatEvent> {

            }
        }
    }
}