package net.kunmc.lab.seihekizinrou

import org.bukkit.entity.*

data class Propensity(
    val player: Player,
    val propensity: String,
) {
    var lastDead: Boolean = false

    var dead: Boolean = false
    var werewolf: Boolean = false
    var votes: MutableList<Player> = mutableListOf()
}