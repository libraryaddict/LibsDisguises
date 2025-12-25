plugins {
    id("nms-module-plugin")
}

description = "v1_21_R6"

nmsModule {
    craftbukkitVersion.set("1.21.10-R0.1-SNAPSHOT")
    javaVersion.set(JavaVersion.VERSION_21)
}

dependencies {
    compileOnly(libs.com.mojang.authlib.old)
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}
