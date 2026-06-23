plugins {
    id("nms-module-plugin")
}

description = "v1_19_R3"

nmsModule {
    craftbukkitVersion.set("1.19.4-R0.1-SNAPSHOT")
    javaVersion.set(JavaVersion.VERSION_17)
    layerFrom.set(":nms:legacy:v1_19_R2")
}

dependencies {
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}
