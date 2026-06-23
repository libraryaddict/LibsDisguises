plugins {
    id("nms-module-plugin")
}

description = "v1_21_R4"

nmsModule {
    craftbukkitVersion.set("1.21.5-R0.1-SNAPSHOT")
    javaVersion.set(JavaVersion.VERSION_21)
    layerFrom.set(":nms:legacy:v1_21_R3")
}

dependencies {
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}
