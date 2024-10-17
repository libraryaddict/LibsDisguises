plugins {
    `java-library`
}

apply(from = rootProject.file("nms/nmsModule.gradle"))

extra["craftbukkitVersion"] = "1.20.3-R0.1-SNAPSHOT"

description = "v1_20_R3"

dependencies {
    compileOnly(libs.com.mojang.authlib.new)
    compileOnly(libs.io.netty.netty.buffer)
    compileOnly(libs.it.unimi.dsi.fastutil)
    compileOnly(libs.com.mojang.datafixerupper)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}