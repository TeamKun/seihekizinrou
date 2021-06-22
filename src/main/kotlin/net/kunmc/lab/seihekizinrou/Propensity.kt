package net.kunmc.lab.seihekizinrou

import org.bukkit.entity.*

data class Propensity(
    val player: Player,
    val propensity: String,
) {
    var werewolf: Boolean = false
    var votes: MutableList<Player> = mutableListOf()
}