plugins {
    id("nms-module-plugin")
}

description = "v1_21_R5"

nmsModule {
    craftbukkitVersion.set("1.21.6-R0.1-SNAPSHOT")
    javaVersion.set(JavaVersion.VERSION_21)
}

dependencies {
    compileOnly(libs.com.mojang.authlib.old)
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}