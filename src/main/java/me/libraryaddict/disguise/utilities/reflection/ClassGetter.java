package me.libraryaddict.disguise.utilities.reflection;

import org.bukkit.entity.Entity;

import java.io.File;
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
    public static ArrayList<Class<?>> getClassesForPackage(String pkgname) {
        return getClassesForPackage(Entity.class, pkgname);
    }

    public static ArrayList<Class<?>> getClassesForPackage(Class runFrom, String pkgname) {
        ArrayList<Class<?>> classes = new ArrayList<>();
        // String relPath = pkgname.replace('.', '/');

        // Get a File object for the package
        CodeSource src = runFrom.getProtectionDomain().getCodeSource();

        if (src != null) {
            URL resource = src.getLocation();

            if (resource.getPath().toLowerCase().endsWith(".jar")) {
                processJarfile(resource, pkgname, classes);
            } else {
                for (File f : new File(resource.getPath() + "/" + pkgname.replace(".", "/")).listFiles()) {
                    if (!f.getName().endsWith(".class") || f.getName().contains("$")) {
                        continue;
                    }

                    try {
                        classes.add(Class.forName(pkgname + "." + f.getName().replace(".class", "")));
                    }
                    catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
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
