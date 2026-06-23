plugins {
    id("nms-module-plugin")
}

description = "v1_18_R1"

nmsModule {
    craftbukkitVersion.set("1.18.1-R0.1-SNAPSHOT")
    javaVersion.set(JavaVersion.VERSION_17)
    layerFrom.set(":nms:legacy:v1_17_R1")
}

dependencies {
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}
