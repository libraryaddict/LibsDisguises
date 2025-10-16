Libs Disguises
=============

One of the best disguise plugins available, this plugin offers an astounding amount of features due to the unique way it implements its use of disguises!

The commonly accepted method of disguising is turning a pig into a cow, but this takes it a step further and allows you to turn that pig, into a dropped piece of diamond armor!

You can modify each disguise to your own liking through use of the API or commands with full tab completion support and the best part is; All of this is free!

There are a few features which you need to pay for, but none of it is needed to enjoy the plugin to its fullest! For further details, read the plugin's description on SpigotMC. You are required to sign in, but you do not need to purchase the plugin.

### Links
* Spigot page: <a href="https://www.spigotmc.org/resources/32453/">Link</a>
* JavaDocs: <a href="https://libraryaddict.github.io/LibsDisguises/javadoc/">Link</a>
* Jenkins Downloads: <a href="https://ci.lib.co.nz/job/LibsDisguises/">Link</a>

### Gradle

For `build.gradle` you can use

```groovy
repositories {
    maven {
        url "https://repo.md-5.net/content/groups/public/"
    }
}

dependencies {
    implementation group: 'me.libraryaddict.disguises', name: 'libsdisguises', version: '11.0.0'
}
```

### Maven

For maven's `pom.xml`, you can use

```xml
<repository>
    <id>md_5-public</id>
    <url>https://repo.md-5.net/content/groups/public/</url>
</repository>

<dependency>
    <groupId>me.libraryaddict.disguises</groupId>
    <artifactId>libsdisguises</artifactId>
    <version>11.0.0</version>
    <scope>provided</scope>
</dependency>
```

NOTE: These versions will NOT work with older versions of Minecraft than 1.12, there is no support for older versions of this plugin.

When posting an issue:<br>
Please make sure you<br>
1) Post a stack trace error, if no stack trace, then post the odd behavior<br>
2) Post the exact steps you used in order to reproduce the issue.<br>
3) Give as much information as possible as to what the issue is and why it occurred so that we can fix it.<br>

Verify that there were no error messages while Lib's Disguises was loading, that you're using the appropiate version of <a href="https://www.spigotmc.org/resources/packetevents-api.80279/">PacketEvents</a>.
If you're using the development builds of Lib's Disguises, make sure you are up to date before reporting a bug.

Important Note
=============

This project does not give permission to modify or bypass code that limits features to paying customers.
Especially not to publish said code.