plugins {
    kotlin("jvm") version "1.5.10"
}

group = "net.kunmc.lab"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://s01.oss.sonatype.org/service/local/repositories/releases/content/")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.destroystokyo.paper", "paper-api", "1.16.5-R0.1-SNAPSHOT")
    implementation("dev.kotx", "flylib-reloaded", "latest.release")
}
