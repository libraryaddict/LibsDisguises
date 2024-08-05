plugins {
    `java-library`
}

apply(from = rootProject.file("nms/nmsModule.gradle"))

extra["craftbukkitVersion"] = "1.20.6-R0.1-SNAPSHOT"

description = "v1_20_R4"
