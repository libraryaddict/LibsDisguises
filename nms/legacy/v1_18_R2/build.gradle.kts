plugins {
    id("nms-module-plugin")
}

description = "v1_18_R2"

nmsModule {
    craftbukkitVersion.set("1.18.2-R0.1-SNAPSHOT")
    javaVersion.set(JavaVersion.VERSION_17)
    layerFrom.set(":nms:legacy:v1_18_R1")
}

dependencies {
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}
