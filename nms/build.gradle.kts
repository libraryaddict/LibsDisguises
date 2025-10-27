plugins {
    id("java")
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.org.spigotmc.spigot.api)
    implementation("org.spigotmc:spigot:1.17.1-R0.1-SNAPSHOT:remapped-mojang")
    compileOnly(libs.io.netty.netty.buffer)
    compileOnly(libs.it.unimi.dsi.fastutil)
    compileOnly(libs.com.mojang.datafixerupper)
    compileOnly(libs.com.retro.packetevents)
}