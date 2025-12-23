plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}


apply(from = rootProject.file("nms/nmsModule.gradle"))
apply(plugin = "io.papermc.paperweight.userdev")

var craftbukkitVersion = "1.21.11-R0.1-SNAPSHOT"
extra["craftbukkitVersion"] = craftbukkitVersion;
extra["spigotServerCode"] = false

description = "v1_21_R7_Paper"

dependencies {
    compileOnly(libs.com.mojang.authlib.new)
    paperweight.paperDevBundle(craftbukkitVersion)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}