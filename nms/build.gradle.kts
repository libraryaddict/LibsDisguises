plugins {
    id("java")
}

dependencies {
    implementation(project(":shared"))
    compileOnly(libs.io.netty.netty.buffer)
    compileOnly(libs.it.unimi.dsi.fastutil)
    compileOnly(libs.com.mojang.datafixerupper)
    compileOnly(libs.com.retro.packetevents)
}

subprojects {
    pluginManager.withPlugin("io.papermc.paperweight.userdev") {
        configurations.named("paperweightDevelopmentBundle") {
            resolutionStrategy.cacheChangingModulesFor(365, TimeUnit.DAYS)
        }

        repositories {
            maven("https://repo.papermc.io/repository/maven-public/") {
                metadataSources {
                    // Prefer cached, fallback if missing
                    artifact()
                    mavenPom()
                }
            }
            mavenCentral {
                metadataSources {
                    artifact()
                    mavenPom()
                }
            }
        }
    }
}