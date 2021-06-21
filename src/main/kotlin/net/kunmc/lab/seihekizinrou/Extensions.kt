package net.kunmc.lab.seihekizinrou

import dev.kotx.flylib.utils.*
import net.kyori.adventure.title.*
import org.bukkit.*
import java.time.*

fun Server.showTitle(
    title: String,
    subTitle: String,
    fadeInSeconds: Int,
    staySeconds: Int,
    fadeOutSeconds: Int,
) {
    showTitle(
        Title.title(
            title.asTextComponent(),
            subTitle.asTextComponent(),
            Title.Times.of(
                Duration.ofSeconds(fadeInSeconds.toLong()),
                Duration.ofSeconds(staySeconds.toLong()),
                Duration.ofSeconds(fadeOutSeconds.toLong()),
            )
        )
    )
}