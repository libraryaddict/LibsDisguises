pluginManagement {
    repositories {
        gradlePluginPortal()

        maven {
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
    }
}

rootProject.name = "LibsDisguises"

include("minimessage")

include("shared", "plugin", "shaded")

val nmsModules = listOf("spigot", "mojang").flatMap { subfolder ->
    File(rootDir, "nms/$subfolder").listFiles()
        ?.filter { it.isDirectory() && it.name.matches("v[\\d_]+R\\d+(_Paper)?".toRegex()) }
        ?.map { ":nms:$subfolder:${it.name}" }
        ?: emptyList()
}

gradle.extra["nmsModules"] = nmsModules
include(nmsModules)
