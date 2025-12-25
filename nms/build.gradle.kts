plugins {
    id("java")
}

dependencies {
    implementation(project(":shared"))
    compileOnly(libs.io.netty.netty.buffer)
    compileOnly(libs.it.unimi.dsi.fastutil)
    compileOnly(libs.com.mojang.datafixerupper)
    compileOnly(libs.com.retro.packetevents)
}
