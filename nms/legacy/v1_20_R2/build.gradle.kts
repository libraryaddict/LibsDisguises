plugins {
    id("nms-module-plugin")
}

description = "v1_20_R2"

nmsModule {
    craftbukkitVersion.set("1.20.2-R0.1-SNAPSHOT")
    javaVersion.set(JavaVersion.VERSION_17)
}

dependencies {
    compileOnly(libs.com.mojang.authlib.old)
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}