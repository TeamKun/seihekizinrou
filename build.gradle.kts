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
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.10")
    implementation("com.destroystokyo.paper", "paper-api", "1.16.5-R0.1-SNAPSHOT")
    compileOnly("dev.kotx", "flylib-reloaded", "0.2.6")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    jar {
        from(configurations.compileOnly.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    create("package") {
        dependsOn(jar)
        doLast {
            copy {
                from(jar)
                into(file("./output"))
            }
        }
    }
}