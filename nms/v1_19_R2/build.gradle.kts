plugins {
    `java-library`
}

apply(from = rootProject.file("nms/nmsModule.gradle"))

extra["craftbukkitVersion"] = "1.19.3-R0.1-SNAPSHOT"

description = "v1_19_R2"

dependencies {
    compileOnly(libs.com.mojang.authlib.old)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}