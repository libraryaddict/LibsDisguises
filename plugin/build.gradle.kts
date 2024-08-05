plugins {
    `maven-publish`
      id("org.hibernate.build.maven-repo-auth") version "3.0.4"
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
        }
        maven {
            name = "md_5-snapshots"
            url = uri("https://repo.md-5.net/content/repositories/snapshots/")
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