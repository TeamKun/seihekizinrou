package net.kunmc.lab.seihekizinrou

import net.kyori.adventure.audience.*
import net.kyori.adventure.text.*
import net.kyori.adventure.title.*
import org.bukkit.*
import org.bukkit.plugin.java.*
import org.bukkit.scheduler.*
import java.time.*

fun Audience.title(
    title: Component,
    subTitle: Component,
    fadeInSeconds: Int,
    staySeconds: Int,
    fadeOutSeconds: Int,
) {
    showTitle(
        Title.title(
            title,
            subTitle,
            Title.Times.of(
                Duration.ofSeconds(fadeInSeconds.toLong()),
                Duration.ofSeconds(staySeconds.toLong()),
                Duration.ofSeconds(fadeOutSeconds.toLong()),
            )
        )
    )
}

fun title(
    title: Component,
    subTitle: Component,
    fadeInSeconds: Int,
    staySeconds: Int,
    fadeOutSeconds: Int,
) {
    SeihekiZinrou.propensities.forEach {
        it.player.title(title, subTitle, fadeInSeconds, staySeconds, fadeOutSeconds)
    }
}

operator fun Component.plus(component: Component) = this.append(component)

fun JavaPlugin.runSync(action: () -> Unit) {
    object : BukkitRunnable() {
        override fun run() {
            action()
        }
    }.runTask(this)
}

fun World.animateTime(plugin: JavaPlugin, target: Int, duration: Int = 60) {
    val diff = if (target - time < 0)
        (target + 26000) - time
    else (target - time)

    val start = time
    var count = 1
    object : BukkitRunnable() {
        override fun run() {
            time = start + ((diff / duration) * count)
            count++
            if (count > duration)
                cancel()
        }
    }.runTaskTimer(plugin, 0, 1)
}