package me.libraryaddict.disguise.utilities;

import me.libraryaddict.disguise.LibsDisguises;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by libraryaddict on 2/06/2017.
 */
public class LibsPremium {
    /**
     * If you're seriously going to modify this to get the premium stuff for free, can you at least not
     * distribute it? You didn't pay for it despite how cheap it is. You spend $8 on a trip to McDonalds
     * but you balk at the idea of actually supporting someone when you can just steal it for free.
     * Is the only reason you don't rob McDonalds because they can catch you? Is the only reason you don't rob your
     * Grandma being that she knows who was in her house? If you see someone's credit card drop out their pocket,
     * you planning on taking it and going shopping?
     * Do you really have the right to give someones work away for free?
     * You know enough to start coding, but you resist the idea of contributing to this plugin. Its even
     * open-source, no one is stopping you. You're the guy who files a bug report because the hacked version has
     * malware installed.
     * I'd hate to work with you.
     */
    private static Boolean thisPluginIsPaidFor;

    /**
     * Don't even think about disabling this unless you purchased the premium plugin. It will uh, corrupt your server
     * and stuff. Also my dog will cry because I can't afford to feed him. And my sister will be beaten by my dad
     * again because I'm not bringing enough money in.
     */
    public static Boolean isPremium() {
        return thisPluginIsPaidFor == null ? !"%%__USER__%%".contains("__USER__") : thisPluginIsPaidFor;
    }

    public static void check(LibsDisguises disguises) {
        thisPluginIsPaidFor = isPremium();

        if (!isPremium() && disguises.getDescription().getVersion().contains("SNAPSHOT")) {
            File[] files = new File("plugins/LibsDisguises/").listFiles();

            if (files == null)
                return;

            for (File file : files) {
                if (!file.isFile())
                    continue;

                if (!file.getName().endsWith(".jar"))
                    continue;

                try {
                    ClassLoader cl = new URLClassLoader(new URL[]{file.toURI().toURL()});
                    Class c = cl.loadClass(LibsPremium.class.getName());

                    Method m = c.getMethod("isPremium");
                    thisPluginIsPaidFor = (Boolean) m.invoke(null);

                    if (isPremium())
                        break;
                }
                catch (Exception ex) {
                    // Don't print off errors
                }
            }
        }
    }
}
