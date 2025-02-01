package me.libraryaddict.disguise.utilities.compiler;

import me.libraryaddict.disguise.utilities.compiler.tasks.CompileJarFileCount;
import me.libraryaddict.disguise.utilities.compiler.tasks.CompileMethods;
import me.libraryaddict.disguise.utilities.compiler.tasks.CompileSounds;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CompileShadedFiles {
    public static void main(String[] args) {
        Path zipFilePath = Paths.get(System.getProperty("jar.path"));

        try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader) null)) {
            Files.write(fs.getPath("/METHOD_MAPPINGS.txt"), new CompileMethods().doMethods());
            Files.write(fs.getPath("/SOUND_MAPPINGS.txt"), new CompileSounds().doSounds());
            // Count after we write the mappings
            Files.write(fs.getPath("/plugin.yml"), new CompileJarFileCount().doFileCount());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
