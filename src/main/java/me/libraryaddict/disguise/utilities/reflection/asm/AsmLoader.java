package me.libraryaddict.disguise.utilities.reflection.asm;

import lombok.Getter;
import me.libraryaddict.disguise.LibsDisguises;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by libraryaddict on 20/02/2020.
 */
@Getter
public class AsmLoader {
    //   private String urlToGrab = "https://repository.ow2.org/nexus/content/repositories/releases/org/ow2/asm/asm/7
    //   .3" +
    //          ".1/asm-7.3.1.jar";
    /**
     * Using maven
     */
    private final String urlToGrab = "https://search.maven.org/remotecontent?filepath=org/ow2/asm/asm/9.1/asm-9.1.jar";
    private final File filePath = new File(LibsDisguises.getInstance().getDataFolder(), "libs/org-ow2-asm-9.1.jar");
    private boolean asmExists;
    private URLClassLoader classLoader;

    public AsmLoader() {
        try {
            Class.forName("org.objectweb.asm.ClassReader");
            asmExists = true;
        } catch (NoClassDefFoundError | ClassNotFoundException ex) {
            // It doesn't exist, good! Lets load it!
        }
    }

    public void loadClassloader() {
        try {
            classLoader = URLClassLoader.newInstance(new URL[]{filePath.toURI().toURL(), LibsDisguises.getInstance().getFile().toURI().toURL()});
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void unload() {
        try {
            classLoader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doDownloadIfRequired() {
        if (!hasASM()) {
            LibsDisguises.getInstance().getLogger().info("Downloading required library for asm!");

            downloadASM();

            LibsDisguises.getInstance().getLogger().info("Downloaded!");
        }
    }

    private boolean hasASM() {
        return filePath.exists();
    }

    private void downloadASM() {
        filePath.getParentFile().mkdirs();

        try (BufferedInputStream in = new BufferedInputStream(new URL(getUrlToGrab()).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(getFilePath())) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
