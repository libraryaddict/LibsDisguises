plugins {
    `java-library`
}

apply(from = rootProject.file("nms/nmsModule.gradle"))

extra["craftbukkitVersion"] = "1.21.10-R0.1-SNAPSHOT";

description = "v1_21_R6"

dependencies {
    compileOnly(libs.com.mojang.authlib.new)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}