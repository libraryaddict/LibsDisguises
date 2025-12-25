plugins {
    id("nms-module-plugin")
}

description = "v1_21_R3"

nmsModule {
    craftbukkitVersion.set("1.21.4-R0.1-SNAPSHOT")
    javaVersion.set(JavaVersion.VERSION_21)
}

dependencies {
    compileOnly(libs.com.mojang.authlib.old)
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}

java {
    sourceCompatibility = nmsModule.javaVersion.get()
    targetCompatibility = nmsModule.javaVersion.get()
}