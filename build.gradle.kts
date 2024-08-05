plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

subprojects {
    repositories {
        mavenCentral()
        mavenLocal()

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
    }

    apply(plugin = "java-library")


    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = "8"
        targetCompatibility = "8"
    }

    group = "me.libraryaddict.disguises"
    version = "1.0-SNAPSHOT"

    dependencies {
        implementation(rootProject.libs.org.spigotmc.spigot.api)

        compileOnly(rootProject.libs.org.projectlombok.lombok)
        annotationProcessor(rootProject.libs.org.projectlombok.lombok)
    }
}