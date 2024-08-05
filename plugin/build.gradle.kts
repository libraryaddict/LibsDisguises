plugins {
    `maven-publish`
}

version = "10.0.44-SNAPSHOT"

dependencies {
    compileOnly(project(":shared"))
    compileOnly(libs.io.netty.netty.buffer)
    compileOnly(libs.io.netty.netty.codec)
    compileOnly(libs.commons.lang.commons.lang)
    compileOnly(libs.com.mojang.authlib.new)
    compileOnly(libs.org.spigotmc.spigot.api)
    compileOnly(libs.com.retro.packetevents)
    compileOnly(libs.libsdisguises.minimessage)
    compileOnly(libs.io.papermc.paper.paper.api);
    compileOnly(libs.it.unimi.dsi.fastutil)
}

publishing {
    repositories {
        maven {
            name = "md_5-releases"
            url = uri("https://repo.md-5.net/content/repositories/releases/")
            credentials {
                username = project.findProperty("repoUser") as String? ?: System.getenv("REPO_USER")
                password = project.findProperty("repoPassword") as String? ?: System.getenv("REPO_PASSWORD")
            }
        }
        maven {
            name = "md_5-snapshots"
            url = uri("https://repo.md-5.net/content/repositories/snapshots/")
            credentials {
                username = project.findProperty("repoUser") as String? ?: System.getenv("REPO_USER")
                password = project.findProperty("repoPassword") as String? ?: System.getenv("REPO_PASSWORD")
            }
        }
        mavenLocal()
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "LibsDisguises"
            artifactId = "LibsDisguises"
        }
    }
}