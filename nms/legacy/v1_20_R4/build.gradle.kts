plugins {
    id("nms-module-plugin")
}

description = "v1_20_R4"

nmsModule {
    craftbukkitVersion.set("1.20.6-R0.1-SNAPSHOT")
    javaVersion.set(JavaVersion.VERSION_21)
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