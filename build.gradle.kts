plugins {
    `java-library`
}

subprojects {
    repositories {
        mavenCentral()

        maven {
            url = uri("https://repo.md-5.net/content/groups/public/")
        }

        maven {
            url = uri("https://papermc.io/repo/repository/maven-public/")
        }

        maven {
            url = uri("https://mvn.lumine.io/repository/maven-public/")
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
        implementation(rootProject.libs.org.spigotmc.spigot.api)

        compileOnly(rootProject.libs.org.projectlombok.lombok)
        annotationProcessor(rootProject.libs.org.projectlombok.lombok)
    }
}
