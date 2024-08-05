plugins {
    alias(libs.plugins.shadowjar)
    `maven-publish`
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
        dependsOn(publish)
        dependsOn(shadowJar)
    }

    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        relocate("net.kyori", "libsdisg.shaded.net.kyori")
        archiveClassifier.set("shadow")
    }
}

publishing {
    publications {
        create<MavenPublication>("relocated") {
            artifact(tasks["shadowJar"]) {
                classifier = null
            }
        }
    }
    repositories {
        mavenLocal()
    }
}