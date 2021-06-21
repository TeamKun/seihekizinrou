package net.kunmc.lab.seihekizinrou

import net.kyori.adventure.text.*
import net.kyori.adventure.title.*
import org.bukkit.*
import java.time.*

fun Server.title(
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