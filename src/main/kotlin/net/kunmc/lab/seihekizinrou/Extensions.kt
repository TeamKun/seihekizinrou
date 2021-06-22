package net.kunmc.lab.seihekizinrou

import net.kyori.adventure.audience.*
import net.kyori.adventure.text.*
import net.kyori.adventure.title.*
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

fun actionbar(component: Component) {
    SeihekiZinrou.propensities.forEach {
        it.player.sendActionBar(component)
    }
}