plugins {
    java
    alias(libs.plugins.linguica)
    alias(libs.plugins.shadow)
}

group = "me.hsgamer.bettereconomy"
version = "4.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

repositories {
    mavenCentral()
    maven {
        name = "rebornmc"
        url = uri("https://repo.rebornmc.it/repository/maven-public/")
    }
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "spigotmc-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        name = "jitpack.io"
        url = uri("https://jitpack.io")
    }
    maven {
        name = "codemc-repo"
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
    maven {
        name = "helpchat-repo-releases"
        url = uri("https://repo.helpch.at/releases")
    }
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.metaverse)
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.vault.api)
    compileOnly(libs.placeholder.api)
    compileOnly(libs.jetbrains.annotations)

    compileOnly(libs.metaverse)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    implementation(libs.hscore.common)
    implementation(libs.hscore.bukkit.utils)
    implementation(libs.hscore.bukkit.config)
    implementation(libs.hscore.config.proxy)

    implementation(libs.minelib.plugin.base)
    implementation(libs.minelib.plugin.listener)
    implementation(libs.minelib.plugin.command)
    implementation(libs.minelib.plugin.permission)
    implementation(libs.minelib.util.subcommand)
    implementation(libs.minelib.scheduler.global)
    implementation(libs.minelib.scheduler.async)

    implementation(libs.topper.data)
    implementation(libs.topper.agent.core)
    implementation(libs.topper.agent.snapshot)
    implementation(libs.topper.agent.storage)
    implementation(libs.topper.storage.sql.conv)
    implementation(libs.topper.spigot.runnable)
}

tasks {
    processResources {
        filesMatching("**/*.yml") {
            expand("version" to project.version.toString())
        }
    }

    shadowJar {
        archiveFileName = "BetterEconomy-${project.version}.jar"
        minimize()

        relocate("me.hsgamer.hscore", "me.hsgamer.bettereconomy.core")
        relocate("org.bstats", "me.hsgamer.bettereconomy.bstats")
        relocate("io.github.projectunified.minelib", "me.hsgamer.bettereconomy.minelib")
        relocate("me.hsgamer.topper", "me.hsgamer.bettereconomy.topper")

        exclude("META-INF/maven/**")
        exclude("META-INF/MANIFEST.MF")

        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }
    }

    assemble {
        dependsOn(shadowJar)
    }
}
