plugins {
    id("nms-module-plugin")
}

description = "v1_21_R7_Folia"

nmsModule {
    craftbukkitVersion.set("1.21.11-R0.1-SNAPSHOT")
    javaVersion.set(JavaVersion.VERSION_21)
}

dependencies {
    implementation(project(":nms:legacy:v1_21_R7"))
    paperweight.foliaDevBundle(nmsModule.craftbukkitVersion.get())
}
