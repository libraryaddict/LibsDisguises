plugins {
    `java-library`
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.3"
}

subprojects {
    repositories {
        mavenCentral()

        maven {
            url = uri("https://mvn.lib.co.nz/spigot/")
        }

        maven {
            url = uri("https://repo.md-5.net/content/groups/public/")
        }

        maven {
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }

        maven {
            url = uri("https://repo.codemc.io/repository/maven-releases/")
        }

        maven {
            url = uri("https://repo.codemc.io/repository/maven-snapshots/")
        }

        maven {
            url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        }
    }

    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.gradle.plugin.idea-ext")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
        disableAutoTargetJvm()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    group = "me.libraryaddict.disguises"
    version = "1.0-SNAPSHOT"

    dependencies {
        compileOnly(rootProject.libs.org.projectlombok.lombok)
        annotationProcessor(rootProject.libs.org.projectlombok.lombok)
    }
}
