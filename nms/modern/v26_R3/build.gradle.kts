plugins {
    id("nms-module-plugin")
}

description = "v26_R3"

nmsModule {
    craftbukkitVersion.set("26.3.build.+")
    javaVersion.set(JavaVersion.VERSION_25)
    layerFrom.set(":nms:modern:v26_R2")
}

dependencies {
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}
