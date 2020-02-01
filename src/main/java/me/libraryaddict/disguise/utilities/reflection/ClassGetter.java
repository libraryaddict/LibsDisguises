package me.libraryaddict.disguise.utilities.reflection;

import org.bukkit.entity.Entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * User: Austin Date: 4/22/13 Time: 11:47 PM (c) lazertester
 */
// Code for this taken and slightly modified from
// https://github.com/ddopson/java-class-enumerator
public class ClassGetter {
    private class TestPrem {
        String user = "%%__USER__%%";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    private @interface PremInfo {
        String user();
    }

    public static ArrayList<Class<?>> getClassesForPackage(String pkgname) {
        ArrayList<Class<?>> classes = new ArrayList<>();
        // String relPath = pkgname.replace('.', '/');

        // Get a File object for the package
        CodeSource src = Entity.class.getProtectionDomain().getCodeSource();

        if (src != null) {
            URL resource = src.getLocation();
            resource.getPath();
            processJarfile(resource, pkgname, classes);
        }

        return classes;
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Unexpected ClassNotFoundException loading class '" + className + "'");
        }
        catch (NoClassDefFoundError e) {
            return null;
        }
    }

    @PremInfo(user = "%%__USER__%%")
    private static void processJarfile(URL resource, String pkgname, ArrayList<Class<?>> classes) {
        try {
            String relPath = pkgname.replace('.', '/');
            String resPath = URLDecoder.decode(resource.getPath(), "UTF-8");
            String jarPath = resPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");

            JarFile jarFile = new JarFile(jarPath);

            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                String className = null;
                if (entryName.endsWith(".class") && entryName.startsWith(relPath) &&
                        entryName.length() > (relPath.length() + "/".length())) {
                    className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
                }
                if (className != null) {
                    Class<?> c = loadClass(className);

                    if (c != null) {
                        classes.add(c);
                    }
                }
            }

            jarFile.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
