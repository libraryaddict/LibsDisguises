plugins {
    `java-library`
}

apply(from = rootProject.file("nms/nmsModule.gradle"))

extra["craftbukkitVersion"] = "1.20-R0.1-SNAPSHOT"

description = "v1_20_R1"

dependencies {
    compileOnly(libs.com.mojang.authlib.old)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}