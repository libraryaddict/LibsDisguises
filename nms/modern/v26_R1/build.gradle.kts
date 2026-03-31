plugins {
    id("nms-module-plugin")
}

description = "v26_R1"

nmsModule {
    craftbukkitVersion.set("26.1-R0.1-SNAPSHOT")
    javaVersion.set(JavaVersion.VERSION_25)
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:${nmsModule.craftbukkitVersion.get()}")
    compileOnly("org.spigotmc:spigot:${nmsModule.craftbukkitVersion.get()}")
}