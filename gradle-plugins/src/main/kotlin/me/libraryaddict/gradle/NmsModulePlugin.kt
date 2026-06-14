package me.libraryaddict.gradle


import io.papermc.paperweight.userdev.PaperweightUserExtension
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RelativePath
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*
import java.util.*

/**
 * Accessed via `nmsModule { ... }`
 */
abstract class NmsModuleExtension {
    abstract val craftbukkitVersion: Property<String>
    abstract val javaVersion: Property<JavaVersion>
}

abstract class NmsModulePlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        val templateDir = rootProject.project(":nms").layout.projectDirectory.dir("src/main/java-templates")
        val defaultProperties =
            rootProject.project(":nms").layout.projectDirectory.file("src/main/resources/default.properties")
        var overrideProperties = layout.projectDirectory.file("src/main/resources/override.properties")
        val libs = project.extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")
        plugins.apply("java-library")
        val nmsModule = extensions.create<NmsModuleExtension>("nmsModule")
        val javaExt = extensions.getByType<JavaPluginExtension>()
        javaExt.toolchain {
            languageVersion.set(nmsModule.javaVersion.map { JavaLanguageVersion.of(it.majorVersion) })
        }

        repositories {
            mavenCentral()
            mavenLocal()
        }

        dependencies {
            "implementation"(project(":shared"))
            "compileOnly"(libs.findLibrary("io-netty-netty-buffer").get())
            "compileOnly"(libs.findLibrary("it-unimi-dsi-fastutil").get())
            "compileOnly"(libs.findLibrary("com-mojang-datafixerupper").get())
            "compileOnly"(libs.findLibrary("com-retro-packetevents").get())
            "compileOnly"(libs.findLibrary("com-mojang-authlib-old").get())
        }

        val generatedSourcesDir = layout.buildDirectory.dir("generated/java/main")
        val generateSharedNmsSources = tasks.register<Sync>("generateSharedNmsSources") {
            group = "build"
            description = "Generates version-specific NMS sources from templates."
            inputs.files(templateDir).withPathSensitivity(PathSensitivity.RELATIVE)
            inputs.files(defaultProperties).withPathSensitivity(PathSensitivity.RELATIVE)
            inputs.files(overrideProperties).optional().withPathSensitivity(PathSensitivity.RELATIVE)
            inputs.property("versionPackage", provider { project.description })
            outputs.dir(generatedSourcesDir)

            val replacements = mutableMapOf<String, Any>()
            doFirst {
                replacements["PACKAGE"] = project.description.toString()
                replacements["NMS_VERSION"] = ""

                val props = Properties().apply {
                    defaultProperties.asFile.inputStream().use(::load)
                    if (overrideProperties.asFile.exists()) {
                        overrideProperties.asFile.inputStream().use(::load)
                    }
                }
                props.forEach { key, value -> replacements[key.toString()] = value }

                if (replacements["NMS_VERSION"].toString().isNotBlank()) {
                    replacements["NMS_VERSION"] = replacements["NMS_VERSION"].toString() + "."
                }
            }

            from(templateDir)
            into(generatedSourcesDir)
            eachFile {
                val basePackagePath = "me/libraryaddict/disguise/utilities/reflection"
                relativePath =
                    RelativePath(true, *"$basePackagePath/${project.description}/$name".split("/").toTypedArray())
            }
            filter(
                mapOf("tokens" to replacements, "beginToken" to "__", "endToken" to "__"),
                ReplaceTokens::class.java
            )
        }

        val usingPaperweight =
            providers.gradleProperty("${project.name}.usePaperweight").map(String::toBoolean).getOrElse(true)
        val isFolia = project.name.endsWith("_Folia")
        val isLegacy = project.name.contains(":legacy:")

        if (usingPaperweight) {
            apply(plugin = "io.papermc.paperweight.userdev")

            val paperweight = project.extensions.getByName("paperweight") as PaperweightUserExtension
            paperweight.reobfArtifactConfiguration =
                io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION

            if (isLegacy) {
                tasks.named("assemble") {
                    dependsOn(tasks.named("reobfJar"))
                }
            }
        }

        if (!isFolia) {

            project.extensions.getByType<SourceSetContainer>().getByName("main").java.srcDir(generatedSourcesDir)

            tasks.named<JavaCompile>("compileJava") {
                dependsOn(generateSharedNmsSources)
            }
        }

        tasks.withType<JavaCompile>().configureEach {
            options.release.set(nmsModule.javaVersion.map {
                it.majorVersion.toInt().coerceAtMost(JavaVersion.VERSION_21.majorVersion.toInt())
            })
        }

        tasks.withType<Jar> { exclude("*.properties") }

        afterEvaluate {
            overrideProperties = (layout.projectDirectory.file("src/main/resources/override.properties"))
        }
    }
}