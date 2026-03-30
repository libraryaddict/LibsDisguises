plugins {
    id("nms-module-plugin")
}

description = "v1_19_R2"

nmsModule {
    craftbukkitVersion.set("1.19.3-R0.1-SNAPSHOT")
    javaVersion.set(JavaVersion.VERSION_17)
}

dependencies {
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}