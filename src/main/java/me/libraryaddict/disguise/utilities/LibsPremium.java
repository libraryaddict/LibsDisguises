package me.libraryaddict.disguise.utilities;

import org.bukkit.configuration.file.YamlConfiguration;

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
                    String pluginVersion;

                    YamlConfiguration config = ReflectionManager.getPluginYaml(cl);

                    pluginVersion = config.getString("version");

                    if (isPremium()) {
                        // Found a premium Lib's Disguises jar (v5.2.6), premium enabled!
                        DisguiseUtilities.getLogger()
                                .info("Found a premium Lib's Disguises jar (v" + pluginVersion + "), premium enabled!");

                        break;
                    } else {
                        // You have a non-premium Lib's Disguises jar (LibsDisguises.jar v5.2.6) in the folder!
                        DisguiseUtilities.getLogger().warning(
                                "You have a non-premium Lib's Disguises jar (" + file.getName() + " v" + pluginVersion +
                                        ") in the folder!");
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    // Don't print off errors
                }
            }
        }
    }
}
