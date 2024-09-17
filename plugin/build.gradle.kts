plugins {
    `maven-publish`
    id("org.hibernate.build.maven-repo-auth") version "3.0.4" apply false
}

version = "10.0.44-SNAPSHOT"

dependencies {
    compileOnly(project(":shared"))
    compileOnly(project(":minimessage", "shadow"))
    compileOnly(libs.io.netty.netty.buffer)
    compileOnly(libs.io.netty.netty.codec)
    compileOnly(libs.commons.lang.commons.lang)
    compileOnly(libs.com.mojang.authlib.new)
    compileOnly(libs.io.papermc.paper.paper.api);
    compileOnly(libs.org.spigotmc.spigot.api)
    compileOnly(libs.com.retro.packetevents)
    compileOnly(libs.it.unimi.dsi.fastutil)
    compileOnly(libs.placeholder.api)
}

tasks.withType<Javadoc>().configureEach {
    title = "LibsDisguises"

    javadocTool.set(
        javaToolchains.javadocToolFor {
            setDestinationDir(file("../build/docs/javadoc"))
            languageVersion = JavaLanguageVersion.of(21)
            include(
                "me/libraryaddict/disguise/*",
                "me/libraryaddict/disguise/disguisetypes/**",
                "me/libraryaddict/disguise/events/**",
                "me/libraryaddict/disguise/utilities/DisguiseUtilities.java",
                "me/libraryaddict/disguise/utilities/SkinUtils.java",
                "me/libraryaddict/disguise/utilities/mineskin/**",
                "me/libraryaddict/disguise/utilities/params/**",
                "me/libraryaddict/disguise/utilities/parser/*",
                "me/libraryaddict/disguise/utilities/reflection/LibsProfileLookup.java",
                "me/libraryaddict/disguise/utilities/reflection/NmsVersion.java",
                "me/libraryaddict/disguise/utilities/reflection/ReflectionManager.java",
                "me/libraryaddict/disguise/utilities/translations/**",
            )
        }
    )
}

publishing {
    repositories {
        // If 'publishToExternalRepo' is false or missing, only publish to local.
        // Else if this is a snapshot build, use the snapshot repo
        // Otherwise, use the release repo
        if (System.getProperty("publishToExternalRepo", "false").equals("false")) {
            mavenLocal();
        } else {
            apply(plugin = "org.hibernate.build.maven-repo-auth")

            if (project.version.toString().contains("-SNAPSHOT")) {
                maven {
                    name = "md_5-snapshots"
                    url = uri("https://repo.md-5.net/content/repositories/snapshots/")
                }
            } else {
                maven {
                    name = "md_5-releases"
                    url = uri("https://repo.md-5.net/content/repositories/releases/")
                }
            }
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            artifactId = "libsdisguises"
        }
    }
}
