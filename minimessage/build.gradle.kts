plugins {
    alias(libs.plugins.shadowjar)
}

dependencies {
    shadow(libs.net.kyori.adventure.api)
    shadow(libs.net.kyori.adventure.text.minimessage)
    shadow(libs.net.kyori.adventure.text.serializer.json)
    shadow(libs.net.kyori.adventure.text.serializer.gson) {
        exclude("*")
    }
}

artifacts.add("shadow", tasks.shadowJar.get())

tasks {
    jar {
        isEnabled = false
    }

    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        relocate("net.kyori", "libsdisg.shaded.net.kyori")
        archiveClassifier.set("shadow")
    }
}