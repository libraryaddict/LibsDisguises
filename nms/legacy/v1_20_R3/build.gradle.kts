plugins {
    id("nms-module-plugin")
}

description = "v1_20_R3"

nmsModule {
    craftbukkitVersion.set("1.20.4-R0.1-SNAPSHOT")
    javaVersion.set(JavaVersion.VERSION_17)
}

dependencies {
    compileOnly(libs.com.mojang.authlib.old)
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get()) {
        isChanging = false
    }
}

java {
    sourceCompatibility = nmsModule.javaVersion.get()
    targetCompatibility = nmsModule.javaVersion.get()
}