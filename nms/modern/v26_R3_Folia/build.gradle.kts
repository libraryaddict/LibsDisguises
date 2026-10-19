plugins {
    id("nms-module-plugin")
}

description = "v26_R3_Folia"

nmsModule {
    craftbukkitVersion.set("26.3.build.+")
    javaVersion.set(JavaVersion.VERSION_25)
}

dependencies {
    implementation(project(":nms:modern:v26_R3"))
    paperweight.foliaDevBundle(nmsModule.craftbukkitVersion.get())
}

// Some dumb gradle bug requires this.
tasks.register("prepareKotlinBuildScriptModel"){}
