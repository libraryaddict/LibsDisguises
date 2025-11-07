import org.json.JSONArray
import org.json.JSONObject
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.net.URI
import java.util.jar.JarFile

plugins {
    `maven-publish`
}

version = "11.0.13-SNAPSHOT"

dependencies {
    compileOnly(project(":shared"))
    compileOnly(project(":minimessage", "shadow"))
    compileOnly(libs.io.netty.netty.buffer)
    compileOnly(libs.io.netty.netty.codec)
    compileOnly(libs.commons.lang.commons.lang)
    compileOnly(libs.com.mojang.authlib.new)
    compileOnly(libs.io.papermc.paper.paper.api);
    compileOnly(libs.com.retro.packetevents)
    compileOnly(libs.it.unimi.dsi.fastutil)
    compileOnly(libs.placeholder.api)
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.org.json)
        classpath(libs.org.ow2.asm)
    }
}

fun getVersionFromName(filename: String): String {
    return """\d+(\.\d+)+(-SNAPSHOT)?""".toRegex().find(filename)?.value
        ?: throw GradleException("Failed to get PE version from: '$filename'")
}

fun extractTimestampFromJar(jar: File): Long {
    JarFile(jar).use { jarFile ->
        val entry = jarFile.getJarEntry("com/github/retrooper/packetevents/util/PEVersions.class")
            ?: throw GradleException("Could not find PEVersions.class in ${jar.name}")

        jarFile.getInputStream(entry).use { inputStream ->
            var timestamp: Long? = null

            ClassReader(inputStream).accept(object : ClassVisitor(Opcodes.ASM9) {
                override fun visitMethod(
                    access: Int,
                    name: String?,
                    descriptor: String?,
                    signature: String?,
                    exceptions: Array<out String>?
                ): MethodVisitor? {
                    if (name == "<clinit>") {
                        return object : MethodVisitor(Opcodes.ASM9) {
                            override fun visitLdcInsn(value: Any?) {
                                if (value is Long) {
                                    timestamp = value
                                }
                            }
                        }
                    }
                    return null
                }
            }, ClassReader.SKIP_DEBUG)

            return timestamp ?: throw GradleException("Could not read PEVersions.class")
        }
    }
}

private fun updateAndWriteProperties(downloadUrl: String) {
    val tempJar = file("${layout.buildDirectory.get()}/tmp/packetevents.jar")
    try {
        println("Downloading ${downloadUrl}...")
        tempJar.parentFile.mkdirs()
        tempJar.writeBytes(URI(downloadUrl).toURL().readBytes())

        val fileName = File(URI(downloadUrl).toURL().path).name
        val version = getVersionFromName(fileName)
        val timestamp = extractTimestampFromJar(tempJar)

        val updaterFile = file("src/main/java/me/libraryaddict/disguise/utilities/updates/PacketEventsUpdater.java")
        println("Updating PacketEventsUpdater.java with version $version and timestamp $timestamp...")
        var content = updaterFile.readText()
        content = content.replaceFirst(
            """(getMinimumPacketEventsVersion\(\)\s*\{[\s\S]*?return ")[^"]+(";)""".toRegex(),
            "$1$version$2"
        )
        content = content.replaceFirst(
            """(return Instant\.ofEpochMilli\()\d+(L\);)""".toRegex(),
            "$1$timestamp$2"
        )
        updaterFile.writeText(content)

        println("Updated class to: '$version' ($timestamp)")
    } finally {
        tempJar.delete()
    }
}

// Update to the latest PacketEvents release
tasks.register("updatePacketEventsRelease") {
    group = "versioning"

    doLast {
        println("Fetching latest PacketEvents from Modrinth...")
        val versionsJson = URI("https://api.modrinth.com/v2/project/packetevents/version").toURL().readText()

        val latestRelease = JSONArray(versionsJson).asSequence()
            .map { it as JSONObject }
            .firstOrNull {
                it.getString("status") == "listed" &&
                        it.getString("version_type") == "release" && it.has("files") &&
                        it.getJSONArray("files").getJSONObject(0).getString("filename").lowercase()
                            .matches(".*(bukkit|spigot|paper).*\\.jar$".toRegex())
            } ?: throw GradleException("Could not find a suitable release version on Modrinth.")

        val fileInfo = latestRelease.getJSONArray("files").getJSONObject(0)
        val downloadUrl = fileInfo.getString("url")

        updateAndWriteProperties(downloadUrl)
    }
}

// Update to the latest PacketEvents build
tasks.register("updatePacketEventsSnapshot") {
    group = "versioning"

    doLast {
        println("Fetching PacketEvents from Jenkins...")
        val buildJson =
            URI("https://ci.codemc.io/job/retrooper/job/packetevents/lastSuccessfulBuild/api/json").toURL().readText()
        val build = JSONObject(buildJson)

        val artifact = build.getJSONArray("artifacts").asSequence()
            .map { it as JSONObject }
            .firstOrNull {
                it.getString("fileName").lowercase().matches(".*(bukkit|spigot|paper).*\\.jar$".toRegex())
            }
            ?: throw GradleException("Could not find JAR in Jenkins.")

        val downloadUrl = "${build.getString("url")}artifact/${artifact.getString("relativePath")}"

        updateAndWriteProperties(downloadUrl)
    }
}

tasks.withType<Javadoc>().configureEach {
    title = "LibsDisguises"

    javadocTool.set(
        javaToolchains.javadocToolFor {
            setDestinationDir(file("../build/docs/javadoc"))
            languageVersion = JavaLanguageVersion.of(21)
            include(
                "me/libraryaddict/disguise/*",
                "me/libraryaddict/disguise/disguisetypes/**",
                "me/libraryaddict/disguise/events/**",
                "me/libraryaddict/disguise/utilities/DisguiseUtilities.java",
                "me/libraryaddict/disguise/utilities/SkinUtils.java",
                "me/libraryaddict/disguise/utilities/mineskin/**",
                "me/libraryaddict/disguise/utilities/params/**",
                "me/libraryaddict/disguise/utilities/parser/*",
                "me/libraryaddict/disguise/utilities/reflection/LibsProfileLookup.java",
                "me/libraryaddict/disguise/utilities/reflection/NmsVersion.java",
                "me/libraryaddict/disguise/utilities/reflection/ReflectionManager.java",
                "me/libraryaddict/disguise/utilities/translations/**",
            )
        }
    )
}

publishing {
    repositories {
        if (System.getenv("MVN_USER") != null) {
            maven {
                val repoType = if (project.version.toString().endsWith("-SNAPSHOT")) "snapshots" else "releases"
                // Eg: https://mvn.lib.co.nz/repositories/maven-%branch%/
                url = uri(System.getenv("MVN_PATH").replace("%branch%", repoType))

                credentials {
                    username = System.getenv("MVN_USER")
                    password = System.getenv("MVN_PASS")
                }
            }
        } else {
            mavenLocal();
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            artifactId = "libsdisguises"
        }
    }
}
