plugins {
    id("nms-module-plugin")
}

description = "v1_20_R1"

nmsModule {
    craftbukkitVersion.set("1.20.1-R0.1-SNAPSHOT")
    javaVersion.set(JavaVersion.VERSION_17)
    layerFrom.set(":nms:legacy:v1_19_R3")
}

dependencies {
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}
