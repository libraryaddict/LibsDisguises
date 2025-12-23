plugins {
    `java-library`
}

apply(from = rootProject.file("nms/nmsModule.gradle"))

extra["craftbukkitVersion"] = "1.17.1-R0.1-SNAPSHOT"

description = "v1_17_R1"

dependencies {
    compileOnly(libs.com.mojang.authlib.old)
}