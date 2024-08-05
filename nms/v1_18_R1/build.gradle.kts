plugins {
    `java-library`
}

apply(from = rootProject.file("nms/nmsModule.gradle"))

extra["craftbukkitVersion"] = "1.18.1-R0.1-SNAPSHOT"

description = "v1_18_R1"
