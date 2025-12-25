pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        includeBuild("gradle-plugins")

        maven {
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
    }
}

rootProject.name = "LibsDisguises"

include("minimessage")

include("shared", "plugin")

val nmsModules = listOf("legacy", "modern").flatMap { subfolder ->
    File(rootDir, "nms/$subfolder").listFiles()
        ?.filter { it.isDirectory() && it.name.matches("v[\\d_]+R\\d+".toRegex()) }
        ?.map { ":nms:$subfolder:${it.name}" }
        ?: emptyList()
}

gradle.extra["nmsModules"] = nmsModules
include(nmsModules)

include("shaded")
