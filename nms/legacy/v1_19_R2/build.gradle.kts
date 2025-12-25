plugins {
    id("nms-module-plugin")
}

description = "v1_19_R2"

nmsModule {
    craftbukkitVersion.set("1.19.3-R0.1-SNAPSHOT")
    javaVersion.set(JavaVersion.VERSION_17)
}

dependencies {
    compileOnly(libs.com.mojang.authlib.old)
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}

java {
    sourceCompatibility = nmsModule.javaVersion.get()
    targetCompatibility = nmsModule.javaVersion.get()
}