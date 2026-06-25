plugins {
    id("nms-module-plugin")
}

description = "v26_R2"

nmsModule {
    craftbukkitVersion.set("26.2.build.+")
    javaVersion.set(JavaVersion.VERSION_25)
    layerFrom.set(":nms:modern:v26_R1")
}

dependencies {
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}
