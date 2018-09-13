package me.libraryaddict.disguise.utilities;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by libraryaddict on 2/06/2017.
 */
public class LibsPremium {
    private static Boolean thisPluginIsPaidFor;

    public static Boolean isPremium() {
        return thisPluginIsPaidFor == null ? !"%%__USER__%%".contains("__USER__") : thisPluginIsPaidFor;
    }

    public static void check(String version) {
        thisPluginIsPaidFor = isPremium();

        if (!isPremium()) {
            File[] files = new File("plugins/LibsDisguises/").listFiles();

            if (files == null)
                return;

            for (File file : files) {
                if (!file.isFile())
                    continue;

                if (!file.getName().endsWith(".jar"))
                    continue;

                try (URLClassLoader cl = new URLClassLoader(new URL[]{file.toURI().toURL()})) {
                    Class c = cl.loadClass(LibsPremium.class.getName());

                    Method m = c.getMethod("isPremium");
                    thisPluginIsPaidFor = (Boolean) m.invoke(null);

                    if (isPremium()) {
                        DisguiseUtilities.getLogger().info("Found a premium Lib's Disguises jar, premium enabled!");

                        break;
                    } else {
                        DisguiseUtilities.getLogger().warning(
                                "You have a non-premium Lib's Disguises jar (" + file.getName() + ") in the folder!");
                    }
                }
                catch (Exception ex) {
                    // Don't print off errors
                }
            }
        }
    }
}
