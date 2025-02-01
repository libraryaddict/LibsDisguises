package me.libraryaddict.disguise.utilities.compiler.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CompileJarFileCount {
    private int getJarFileCount(File file, String... skipFiles) {
        try (JarFile jar = new JarFile(file)) {
            int count = 0;

            Enumeration<JarEntry> entries = jar.entries();

            loop:
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                if (entry.isDirectory()) {
                    continue;
                }

                for (String skipFile : skipFiles) {
                    if (!skipFile.equals(entry.getName())) {
                        continue;
                    }

                    continue loop;
                }

                count++;
            }

            return count;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] doFileCount() {
        int totalCount = getJarFileCount(new File(System.getProperty("jar.path")), "METHOD_MAPPINGS.txt", "SOUND_MAPPINGS.txt") + 2;

        try {
            Path path = new File(new File("build/resources/main"), "plugin.yml").toPath();
            String pluginYaml =
                Files.readString(path, StandardCharsets.UTF_8).replaceFirst("file-count: -?\\d+", "file-count: " + totalCount);
            return pluginYaml.getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
