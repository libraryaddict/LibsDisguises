plugins {
    id("nms-module-plugin")
}

description = "v26_R1"

nmsModule {
    craftbukkitVersion.set("26.1.2.build.+")
    javaVersion.set(JavaVersion.VERSION_25)
    layerFrom.set(":nms:legacy:v1_21_R7")
}

dependencies {
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}
