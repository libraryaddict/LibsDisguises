plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("io.papermc.paperweight.userdev:io.papermc.paperweight.userdev.gradle.plugin:2.0.0-beta.19")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
   }
}

gradlePlugin {
    plugins {
        create("nms-module-plugin") {
            id = "nms-module-plugin"
            implementationClass = "me.libraryaddict.gradle.NmsModulePlugin"
        }
    }
}