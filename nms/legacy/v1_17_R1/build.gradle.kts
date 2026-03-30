plugins {
    id("nms-module-plugin")
}

description = "v1_17_R1"

nmsModule {
    craftbukkitVersion.set("1.17.1-R0.1-SNAPSHOT")
    javaVersion.set(JavaVersion.VERSION_17)
}

dependencies {
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}