package me.libraryaddict.disguise.utilities.reflection;

import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.entity.Entity;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
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

    public static ArrayList<String> getEntriesForPackage(String pkgname) {
        return getEntriesForPackage(Entity.class, pkgname);
    }

    public static ArrayList<Class<?>> getClassesForPackage(Class runFrom, String pkgname) {
        ArrayList<String> list = getEntriesForPackage(runFrom, pkgname);
        ArrayList<Class<?>> classList = new ArrayList<>();

        for (String s : list) {
            if (!s.endsWith(".class")) {
                continue;
            }

            Class<?> c = loadClass(s.replace(".class", "").replace('/', '.'));

            if (c != null) {
                classList.add(c);
            }
        }

        return classList;
    }

    public static ArrayList<String> getEntriesForPackage(Class runFrom, String pkgname) {
        ArrayList<String> classes = new ArrayList<>();
        // String relPath = pkgname.replace('.', '/');

        // Get a File object for the package
        CodeSource src = runFrom.getProtectionDomain().getCodeSource();

        if (src != null) {
            URL resource = src.getLocation();
            String path = resource.getPath().toLowerCase(Locale.ENGLISH);

            boolean isInsideJar = path.endsWith(".jar") || (path.contains(".jar!") && path.endsWith(".class"));

            if (isInsideJar) {
                processJarfile(resource, pkgname, classes);
            } else {
                File[] baseFileList = new File(resource.getPath() + "/" + pkgname.replace(".", "/")).listFiles();

                if (baseFileList != null) {
                    for (File f : baseFileList) {
                        if (f.getName().contains("$")) {
                            continue;
                        }

                        classes.add(pkgname + "/" + f.getName());
                    }
                } else {
                    DisguiseUtilities.getLogger().severe("File not found for: " + resource.getPath() + "/" + pkgname.replace(".", "/"));
                }
            }
        }

        return classes;
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            return null;
        }
    }

    private static void processJarfile(URL resource, String pkgname, ArrayList<String> classes) {
        try {
            String relPath = pkgname.replace('.', '/');
            String resPath = URLDecoder.decode(resource.getPath(), "UTF-8");
            String jarPath = resPath.replaceFirst("\\.jar!.*", ".jar").replaceFirst("file:", "");

            JarFile jarFile = new JarFile(jarPath);

            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                String className = null;

                if (entryName.startsWith(relPath) && entryName.length() > (relPath.length() + "/".length())) {
                    className = entryName.replace('\\', '/');
                }

                if (className != null) {
                    classes.add(className);
                }
            }

            jarFile.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
