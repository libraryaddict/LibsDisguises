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
        configurations.matching { it.name.contains("paperweight") }.configureEach {
            resolutionStrategy {
                eachDependency {
                    if (requested.version?.endsWith("-SNAPSHOT") == true) {
                        // Always prefer cached version, but allow download if missing
                        useVersion(requested.version!!)
                    }
                }
            }
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