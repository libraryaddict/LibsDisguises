plugins {
    `java-library`
}

apply(from = rootProject.file("nms/nmsModule.gradle"))

extra["craftbukkitVersion"] = "1.19.1-R0.1-SNAPSHOT"

description = "v1_19_R1"
