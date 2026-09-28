plugins {
    id("nms-module-plugin")
}

description = "v26_R2_Folia"

nmsModule {
    craftbukkitVersion.set("26.2.build.+")
    javaVersion.set(JavaVersion.VERSION_25)
}

dependencies {
    implementation(project(":nms:modern:v26_R2"))
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}

// Some dumb gradle bug requires this.
tasks.register("prepareKotlinBuildScriptModel") {}
