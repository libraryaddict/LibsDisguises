plugins {
    id("nms-module-plugin")
}

description = "v1_20_R4"

nmsModule {
    craftbukkitVersion.set("1.20.6-R0.1-SNAPSHOT")
    javaVersion.set(JavaVersion.VERSION_21)
    layerFrom.set(":nms:legacy:v1_20_R3")
}

dependencies {
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}
