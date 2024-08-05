import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    `java`
    alias(libs.plugins.shadowjar)
    application
}

tasks {
    build {
        dependsOn("shadowJar")
        dependsOn("run")
        dependsOn(getByName("jenkins"))
    }

    task("publish") {
        dependsOn("build")
    }

    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        exclude("**/CompileMethods.class")
    }

    getByName("run") {
        mustRunAfter(shadowJar)
    }

    task("jenkins") {
        mustRunAfter("run")

        doLast {
            copy {
                from(shadowJar.get().archiveFile.get().asFile.absolutePath)
                into(rootProject.projectDir.absolutePath + "\\target")
                rename {
                    "LibsDisguises.jar"
                }
            }
        }
    }

    processResources {
        // Always inject timestamp & version
        outputs.upToDateWhen { false }

        var number = "unknown";

        if (gradle.startParameter.taskNames.contains("publish")) {
            number = System.getProperty("build.number", "unknown");
        }

        filesMatching("plugin.yml") {
            expand(
                "libsdisguisesVersion" to project(":plugin").version,
                "timestamp" to DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(LocalDateTime.now()),
                "buildNumber" to number
            )
        }
    }
}

application {
    mainClass = "me.libraryaddict.disguise.utilities.watchers.CompileMethods"
    applicationDefaultJvmArgs = listOf(
        "-Djar.path=" + tasks.named<ShadowJar>("shadowJar").get().archiveFile.get().asFile.absolutePath
    )
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}

dependencies {
    shadow(libs.libsdisguises.minimessage) {
        exclude("*")
    }
    shadow(project(":shared")) {
        exclude("*")
    }
    shadow(project(":plugin")) {
        exclude("*")
    }
    runtimeOnly(project(":plugin"))
    runtimeOnly(libs.com.retro.packetevents)

    (gradle.extra["nmsModules"] as List<*>).map { s -> project(s as String) }.forEach {
        shadow(it) {
            exclude("*")
        }
    }

    testCompileOnly(libs.org.projectlombok.lombok)
    testAnnotationProcessor(libs.org.projectlombok.lombok)

    testImplementation(project(":shared"))
    testImplementation(project(":plugin"))
    testImplementation(libs.libsdisguises.minimessage)
    testImplementation(libs.mockito)
    testImplementation(libs.com.retro.packetevents)
    testImplementation(libs.net.kyori.adventure.api)
    testImplementation(libs.net.kyori.adventure.text.minimessage)
    testImplementation(libs.net.kyori.adventure.text.serializer.gson)
    testImplementation(libs.net.kyori.adventure.text.serializer.json)
    testRuntimeOnly(libs.org.spigotmc.spigot.api)
    testRuntimeOnly(libs.org.spigotmc.spigot)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}