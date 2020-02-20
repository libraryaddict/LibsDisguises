package me.libraryaddict.disguise.utilities.reflection.asm;

import lombok.AccessLevel;
import lombok.Getter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by libraryaddict on 20/02/2020.
 */
@Getter(value = AccessLevel.PRIVATE)
public class AsmDownloader {
    //   private String urlToGrab = "https://repository.ow2.org/nexus/content/repositories/releases/org/ow2/asm/asm/7
    //   .3" +
    //          ".1/asm-7.3.1.jar";
    /**
     * Using maven
     */
    private String urlToGrab = "https://search.maven.org/remotecontent?filepath=org/ow2/asm/asm/7.3.1/asm-7.3.1.jar";
    private File filePath = new File(LibsDisguises.getInstance().getDataFolder(), "libs/org-ow2-asm-7.3.1.jar");

    public AsmDownloader() {
        if (NmsVersion.v1_13.isSupported()) {
            throw new IllegalStateException("Sorry, this shouldn't have been started!");
        }

        try {
            Class.forName("org.objectweb.asm.ClassReader");
            return;
        }
        catch (NoClassDefFoundError | ClassNotFoundException ex) {
            // It doesn't exist, good! Lets load it!
        }

        if (!hasASM()) {
            LibsDisguises.getInstance().getLogger().info("Downloading required library for 1.12 support!");

            downloadASM();

            LibsDisguises.getInstance().getLogger().info("Downloaded!");
        }

        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(getClass().getClassLoader(), filePath.toURI().toURL());
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private boolean hasASM() {
        return filePath.exists();
    }

    private void downloadASM() {
        filePath.getParentFile().mkdirs();

        try (BufferedInputStream in = new BufferedInputStream(
                new URL(getUrlToGrab()).openStream()); FileOutputStream fileOutputStream = new FileOutputStream(
                getFilePath())) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
