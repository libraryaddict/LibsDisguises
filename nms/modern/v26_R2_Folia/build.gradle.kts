plugins {
    id("nms-module-plugin")
}

description = "v26_R2_Folia"

nmsModule {
    craftbukkitVersion.set("26.2.build.60-beta")
    javaVersion.set(JavaVersion.VERSION_25)
}

dependencies {
    implementation(project(":nms:modern:v26_R2"))
    // Folia does not publish a 26.2 development bundle yet. This adapter only
    // uses Moonrise's tracked-entity API, which is part of the Paper bundle too.
    paperweight.paperDevBundle(nmsModule.craftbukkitVersion.get())
}

// Some dumb gradle bug requires this.
tasks.register("prepareKotlinBuildScriptModel") {}
