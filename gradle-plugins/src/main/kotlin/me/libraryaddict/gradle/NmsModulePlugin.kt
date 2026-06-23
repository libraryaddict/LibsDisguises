package me.libraryaddict.gradle

import io.papermc.paperweight.userdev.PaperweightUserExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*
import java.io.File

abstract class NmsModuleExtension {
    abstract val craftbukkitVersion: Property<String>
    abstract val javaVersion: Property<JavaVersion>
    abstract val layerFrom: Property<String>
}

abstract class NmsModulePlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        val libs = extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")
        plugins.apply("java-library")
        val nmsModule = extensions.create<NmsModuleExtension>("nmsModule")
        extensions.getByType<JavaPluginExtension>().toolchain {
            languageVersion.set(nmsModule.javaVersion.map { JavaLanguageVersion.of(it.majorVersion) })
        }

        repositories {
            mavenCentral()
            mavenLocal()
        }

        dependencies {
            "implementation"(project(":shared"))
            sequenceOf(
                "io-netty-netty-buffer",
                "it-unimi-dsi-fastutil",
                "com-mojang-datafixerupper",
                "com-retro-packetevents",
                "com-mojang-authlib-old"
            ).forEach { libName ->
                "compileOnly"(libs.findLibrary(libName).get())
            }
        }

        val generatedLayeredSourcesDir = layout.buildDirectory.dir("generated/layered/main")
        val generateLayeredNmsSources = tasks.register("generateLayeredNmsSources") {
            group = "build"
            outputs.dir(generatedLayeredSourcesDir)

            doLast {
                val outputDir = generatedLayeredSourcesDir.get().asFile
                outputDir.deleteRecursively()
                outputDir.mkdirs()

                val targetVersion = project.description.toString()
                val localSourceRoot = layout.projectDirectory.dir("src/main/java").asFile
                val targetParentVersion = nmsModule.layerFrom.orNull?.trim().orEmpty()
                val parentProject = targetParentVersion.takeIf { it.isNotEmpty() }?.let { rootProject.project(it) }
                val parentEffectiveSourceRoot = parentProject
                    ?.layout?.buildDirectory?.dir("generated/layered/main")?.get()?.asFile

                val files = linkedMapOf<String, String>()

                parentEffectiveSourceRoot?.let { parentRoot ->
                    if (parentRoot.exists()) {
                        val parentVersion = parentProject.description.toString()
                        parentRoot.walkTopDown().filter { it.isFile }.forEach { file ->
                            files[file.relativeTo(parentRoot).invariantSeparatorsPath.replace(
                                parentVersion,
                                targetVersion
                            )] = file.readText().replace(parentVersion, targetVersion)
                        }
                    }
                }

                // Overlay existing module, skips if missing
                if (localSourceRoot.exists()) {
                    localSourceRoot.walkTopDown().filter { it.isFile }.forEach { file ->
                        val path = file.relativeTo(localSourceRoot).invariantSeparatorsPath
                        val incoming = file.readText()
                        files[path] =
                            if (files.containsKey(path) && path.endsWith(".java")) {
                                mergeJavaSource(files[path]!!, incoming)
                            } else incoming
                    }
                }

                files.forEach { (relativePath, content) ->
                    File(outputDir, relativePath).apply { parentFile.mkdirs() }.writeText(content)
                }
            }
        }

        val usingPaperweight = providers.gradleProperty("${project.name}.usePaperweight")
            .map(String::toBoolean).getOrElse(true)

        if (usingPaperweight) {
            apply(plugin = "io.papermc.paperweight.userdev")
            val paperweight = project.extensions.getByName("paperweight") as PaperweightUserExtension
            paperweight.reobfArtifactConfiguration =
                io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION
            if (project.path.contains(":legacy:")) {
                tasks.named("assemble") { dependsOn(tasks.named("reobfJar")) }
            }
        }

        tasks.withType<JavaCompile>().configureEach {
            options.release.set(nmsModule.javaVersion.map {
                it.majorVersion.toInt().coerceAtMost(JavaVersion.VERSION_21.majorVersion.toInt())
            })
        }

        afterEvaluate {
            if (!nmsModule.layerFrom.isPresent) return@afterEvaluate

            tasks.named("generateLayeredNmsSources") {
                dependsOn(rootProject.project(nmsModule.layerFrom.get()).tasks.named("generateLayeredNmsSources"))
            }

            project.extensions.getByType<SourceSetContainer>().getByName("main").java.setSrcDirs(
                listOf(
                    generatedLayeredSourcesDir.get().asFile
                )
            )

            tasks.named<JavaCompile>("compileJava") { dependsOn(generateLayeredNmsSources) }
        }
    }
}


private fun mergeJavaSource(base: String, overlay: String): String {
    val overlayImports = overlay.extractImports()
    var merged = base

    // Some modules may not bother declaring a file!
    val (bodyStart, bodyEnd) = merged.findPrimaryClassBody() ?: return merged
    val overlayRange = overlay.findPrimaryClassBody() ?: return merged

    val baseBody = merged.substring(bodyStart + 1, bodyEnd)
    val overlayBody = overlay.substring(overlayRange.first + 1, overlayRange.second)
    val className = merged.findPrimaryClassName() ?: return merged

    val baseMembers = baseBody.splitTopLevelMembers()
    val overlayMembers = overlayBody.splitTopLevelMembers()

    val indexByKey =
        baseMembers.mapIndexedNotNull { i, m -> m.memberKey(className)?.let { it to i } }.toMap().toMutableMap()
    val mergedMembers = baseMembers.toMutableList()

    for (member in overlayMembers) {
        val key = member.memberKey(className)
        if (key != null && indexByKey.containsKey(key)) {
            val targetIdx = indexByKey.getValue(key)
            if (mergedMembers[targetIdx].normalizeMember() != member.normalizeMember()) {
                mergedMembers[targetIdx] = member
            }
        } else {
            mergedMembers += member
            key?.let { indexByKey[it] = mergedMembers.lastIndex }
        }
    }

    val rebuiltBody = buildString {
        mergedMembers.forEachIndexed { i, m ->
            if (i > 0 && !endsWith("\n\n")) append('\n')
            append(m)
            append('\n')
        }
    }

    merged = merged.substring(0, bodyStart + 1) + "\n" + rebuiltBody + merged.substring(bodyEnd)
    return merged.pruneUnusedImports(overlayImports.toSet(), overlayImports).pruneUnusedPrivateFields()
}

private fun String.extractImports() =
    lineSequence().map { it.trim() }.filter { it.startsWith("import ") && it.endsWith(";") }.distinct().toList()

private fun String.pruneUnusedPrivateFields(): String {
    val (bodyStart, bodyEnd) = findPrimaryClassBody() ?: return this
    val body = substring(bodyStart + 1, bodyEnd)
    val members = body.splitTopLevelMembers()
    val fieldRegex = Regex(
        """^\s*private\s+(?:final\s+)?(?:transient\s+)?(?:volatile\s+)?[\w<>\[\],\s]+\s+([A-Za-z_]\w*)\s*(?:=[^;]*)?;$"""
    )
    val toRemove = members.indices.filter { i ->
        val fieldName = fieldRegex.find(members[i].trimStart())?.groupValues?.getOrNull(1) ?: return@filter false
        members.none { other -> other != members[i] && other.contains(Regex("\\b${Regex.escape(fieldName)}\\b")) }
    }

    if (toRemove.isEmpty()) return this
    val rebuiltBody = buildString {
        members.forEachIndexed { i, m ->
            if (i in toRemove) return@forEachIndexed
            if (isNotEmpty() && !endsWith("\n\n")) append('\n')
            append(m)
            append('\n')
        }
    }
    return substring(0, bodyStart + 1) + "\n" + rebuiltBody + substring(bodyEnd)
}

private fun String.normalizeMember() = replace(Regex("""//.*"""), "")
    .replace(Regex("""/\*.*?\*/""", RegexOption.DOT_MATCHES_ALL), "")
    .replace(Regex("""\s+"""), " ")
    .trim()

private fun String.pruneUnusedImports(
    preferredImports: Set<String> = emptySet(),
    extraImports: List<String> = emptyList()
): String {
    val imports = (extractImports() + extraImports).distinct()
    if (imports.isEmpty()) return this
    val body = lineSequence()
        .filterNot { val t = it.trimStart(); t.startsWith("package ") || t.startsWith("import ") }
        .joinToString("\n")
    val used = imports.filter { line ->
        val fqcn = line.removeSurrounding("import ", ";").trim()
        fqcn.startsWith("static ") || fqcn.endsWith(".*") || body.contains(
            Regex(
                "\\b${
                    Regex.escape(
                        fqcn.substringAfterLast(
                            '.'
                        )
                    )
                }\\b"
            )
        )
    }
    val kept = used.groupBy { it.substringAfterLast('.').substringBefore(';') }.values.map { collisions ->
        if (collisions.size == 1) collisions.first()
        else collisions.firstOrNull { it in preferredImports }
            ?: collisions.firstOrNull { !it.contains(Regex("\\.[vV]\\d+_R\\d+")) }
            ?: collisions.first()
    }
    return replaceImportBlock(kept)
}

private fun String.replaceImportBlock(imports: List<String>): String {
    val block = imports.distinct().sorted().joinToString("\n")
    var importStart = -1
    var importEnd = -1
    var offset = 0
    for (line in lines()) {
        val trimmed = line.trim()
        if (trimmed.startsWith("import ") && trimmed.endsWith(";")) {
            if (importStart < 0) importStart = offset
            importEnd = offset + line.length + 1
        } else if (importStart >= 0 && trimmed.isNotEmpty()) break
        offset += line.length + 1
    }
    val (before, after) = if (importStart >= 0) {
        substring(0, importStart) to substring(importEnd).trimStart('\n')
    } else {
        val packageEnd = Regex("""^\s*package\s+[^;]+;""").find(this)?.range?.last?.plus(1) ?: 0
        substring(0, packageEnd) to substring(packageEnd).trimStart('\n')
    }
    val separator = if (block.isBlank()) "" else "\n\n"
    return buildString {
        append(before.trimEnd())
        append(separator)
        append(block)
        if (block.isNotBlank() && after.isNotBlank()) append("\n\n") else if (after.isNotBlank()) append("\n")
        append(after)
    }
}

private fun String.findPrimaryClassBody(): Pair<Int, Int>? {
    val classIdx = Regex("""\b(class|enum|interface)\b""").find(this)?.range?.first ?: return null
    val open = indexOf('{', classIdx)
    if (open < 0) return null
    var depth = 0
    for (i in open until length) {
        when (this[i]) {
            '{' -> depth++
            '}' -> {
                depth--; if (depth == 0) return open to i
            }
        }
    }
    return null
}

private fun String.findPrimaryClassName(): String? =
    Regex("""\b(class|enum|interface)\s+([A-Za-z_]\w*)""").find(this)?.groupValues?.getOrNull(2)

private fun String.splitTopLevelMembers(): List<String> = buildList {
    var i = 0
    while (i < length) {
        while (i < length && this@splitTopLevelMembers[i].isWhitespace()) i++
        if (i >= length) break
        val start = i
        var depth = 0
        var inString = false
        var stringQuote = '"'
        var inLineComment = false
        var inBlockComment = false
        while (i < length) {
            val ch = this@splitTopLevelMembers[i]
            val next = if (i + 1 < length) this@splitTopLevelMembers[i + 1] else '"'
            when {
                inLineComment -> {
                    if (ch == '\n') inLineComment = false
                    i++
                }

                inBlockComment -> {
                    if (ch == '*' && next == '/') {
                        inBlockComment = false; i += 2
                    } else i++
                }

                inString -> {
                    if (ch == '\\') i += 2 else {
                        if (ch == stringQuote) inString = false; i++
                    }
                }

                ch == '/' && next == '/' -> {
                    inLineComment = true; i += 2
                }

                ch == '/' && next == '*' -> {
                    inBlockComment = true; i += 2
                }

                ch == '"' && next == '"' && i + 2 < length && this@splitTopLevelMembers[i + 2] == '"' -> {
                    i += 3
                    while (i + 2 < length) {
                        if (this@splitTopLevelMembers[i] == '"' && this@splitTopLevelMembers[i + 1] == '"' && this@splitTopLevelMembers[i + 2] == '"') {
                            i += 3; break
                        }
                        i++
                    }
                }

                ch == '"' || ch == '\'' -> {
                    inString = true; stringQuote = ch; i++
                }

                ch == '{' -> {
                    depth++; i++
                }

                ch == '}' -> {
                    if (depth == 0) {
                        i++; break
                    }
                    depth--
                    if (depth == 0) {
                        i++; break
                    }
                    i++
                }

                ch == ';' && depth == 0 -> {
                    i++; break
                }

                else -> i++
            }
        }
        val member = substring(start, i).trimEnd()
        if (member.isNotBlank()) add(member)
    }
}


private fun String.memberKey(className: String): String? {
    // TODO Probably breaks on lombok
    val compact = replace(Regex("""\s+"""), " ").trim()
    val methodMatch = Regex("""([A-Za-z_]\w*)\s*\(([^)]*)\)\s*(?:throws\s+[^{]+)?\{""").find(compact)
    if (methodMatch != null) {
        val (methodName, paramsStr) = methodMatch.destructured
        val params = paramsStr.split(',').joinToString(",") { param ->
            val cleaned = param.replace(Regex("""@\w+(\([^)]*\))?\s*"""), "")
                .replace("final ", "").trim()
            val tokens = cleaned.split(Regex("""\s+"""))
            if (tokens.size <= 1) cleaned else tokens.dropLast(1).joinToString(" ")
        }
        val prefix = if (methodName == className) "C" else "M"
        return "$prefix:$methodName($params)"
    }
    val fieldMatch = Regex("""([A-Za-z_]\w*)\s*(?:=[^;]*)?;$""").find(compact)
    return fieldMatch?.let { "F:${it.groupValues[1]}" }
}