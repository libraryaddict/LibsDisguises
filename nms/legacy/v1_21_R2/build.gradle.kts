plugins {
    id("nms-module-plugin")
}

description = "v1_21_R2"

nmsModule {
    craftbukkitVersion.set("1.21.3-R0.1-SNAPSHOT")
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