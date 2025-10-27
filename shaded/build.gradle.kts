import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    java
    alias(libs.plugins.shadowjar)
    application
}

tasks {
    build {
        dependsOn("shadowJar")
        dependsOn("run")
        //dependsOn(getByName("proguard"))
        dependsOn(getByName("jenkins"))
    }

    register("publish") {
        dependsOn("build")
    }

    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        exclude("me/libraryaddict/disguise/utilities/compiler/**")
        exclude("META-INF/**")
    }

    getByName("run") {
        mustRunAfter(shadowJar)
    }

    register<proguard.gradle.ProGuardTask>("proguard") {
        verbose()

        // Alternatively put your config in a separate file
        // configuration("config.pro")

        // Use the jar task output as a input jar. This will automatically add the necessary task dependency.
        injars(named("shadowJar"))

        outjars("build/proguard-obfuscated.jar")

        val javaHome = System.getProperty("java.home")
        // Automatically handle the Java version of this build.
        if (System.getProperty("java.version").startsWith("1.")) {
            // Before Java 9, the runtime classes were packaged in a single jar file.
            libraryjars("$javaHome/lib/rt.jar")
        } else {
            // As of Java 9, the runtime classes are packaged in modular jmod files.
            libraryjars(
                // filters must be specified first, as a map
                mapOf(
                    "jarfilter" to "!**.jar",
                    "filter" to "!module-info.class"
                ),
                "$javaHome/jmods/java.base.jmod"
            )
        }

        allowaccessmodification()
        dontwarn()
        dontnote()
        dontobfuscate()
        dontshrink()
        dontpreverify()

        printmapping("build/proguard-mapping.txt")
    }

    register("jenkins") {
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

        filesMatching("plugin.yml") {
            val buildHash by lazy {
                val hashStdout = providers.exec {
                    commandLine("git", "rev-parse", "--short", "HEAD")
                }
                val hash = hashStdout.toString().trim()

                val statusStdout = providers.exec {
                    commandLine("git", "status", "--porcelain")
                }.result.get()
                val uncommittedCount = statusStdout.toString().trim().lines().count { it.isNotBlank() }

                if (uncommittedCount > 0) {
                    "$hash~$uncommittedCount"
                } else {
                    hash
                }
            }

            expand(
                "libsdisguisesVersion" to project(":plugin").version,
                "timestamp" to DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(LocalDateTime.now()),
                "buildNumber" to System.getProperty("build.number", "unknown"),
                "buildHash" to buildHash
            )
        }
    }
}

application {
    mainClass = "me.libraryaddict.disguise.utilities.compiler.CompileShadedFiles"
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
    shadow(project(":minimessage", "shadow")) {
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

    implementation(rootProject.libs.io.papermc.paper.paper.api)

    // Dependencies that are used to compile, and will also be provided at test runtime
    testImplementation(project(":shared"))
    testImplementation(project(":plugin"))
    testImplementation(project(":minimessage", "shadow"))
    testImplementation(libs.mockito)
    testImplementation(libs.com.retro.packetevents)
    testImplementation(libs.net.kyori.adventure.api)
    testImplementation(libs.net.kyori.adventure.text.minimessage)
    testImplementation(libs.net.kyori.adventure.text.serializer.gson)
    testImplementation(libs.net.kyori.adventure.text.serializer.json)

    // dependencies that are only used when running the tests
    testRuntimeOnly(rootProject.libs.io.papermc.paper.paper.api)
    //testRuntimeOnly(libs.org.spigotmc.spigot)
    testRuntimeOnly(libs.commons.lang.commons.lang)
    testRuntimeOnly(libs.io.netty.netty.buffer)
    testRuntimeOnly(libs.io.netty.netty.codec)
    testRuntimeOnly(libs.com.mojang.authlib.new)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

buildscript {
    dependencies {
        classpath(libs.com.guardsquare.proguard)
    }
}