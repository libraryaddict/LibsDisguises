package me.libraryaddict.disguise.utilities.reflection.asm;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Created by libraryaddict on 15/05/2021.
 */
public class LibsJarFile extends JarFile {
    private final HashMap<String, byte[]> customFiles = new HashMap<>();

    public LibsJarFile(File file) throws IOException {
        super(file);
    }

    @Override
    public synchronized InputStream getInputStream(ZipEntry ze) throws IOException {
        if (customFiles.containsKey(ze.getName())) {
            return new ByteArrayInputStream(customFiles.get(ze.getName()));
        }

        return super.getInputStream(ze);
    }

    public void addClass(String name, byte[] bytes) {
        customFiles.put(name, bytes);
    }

    @Override
    public void close() throws IOException {
        customFiles.clear();
        super.close();
    }
}
