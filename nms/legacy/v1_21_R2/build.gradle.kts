plugins {
    id("nms-module-plugin")
}

description = "v1_21_R2"

nmsModule {
    craftbukkitVersion.set("1.21.3-R0.1-SNAPSHOT")
    javaVersion.set(JavaVersion.VERSION_21)
    layerFrom.set(":nms:legacy:v1_21_R1")
}

dependencies {
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}
