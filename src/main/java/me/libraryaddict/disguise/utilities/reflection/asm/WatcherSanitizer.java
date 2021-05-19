package me.libraryaddict.disguise.utilities.reflection.asm;

import com.google.gson.Gson;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by libraryaddict on 17/02/2020.
 */
public class WatcherSanitizer {
    public static void checkPreLoaded() throws NoSuchFieldException, IllegalAccessException {
        JavaPluginLoader javaLoader = (JavaPluginLoader) LibsDisguises.getInstance().getPluginLoader();

        Field lM = JavaPluginLoader.class.getDeclaredField("loaders");
        lM.setAccessible(true);
        List loaders = (List) lM.get(javaLoader);

        Field lF = WatcherSanitizer.class.getClassLoader().getClass().getDeclaredField("classes");
        lF.setAccessible(true);
        Field dF = WatcherSanitizer.class.getClassLoader().getClass().getDeclaredField("description");
        dF.setAccessible(true);

        for (Object loader : loaders) {
            Map<String, Class<?>> lClasses = (Map<String, Class<?>>) lF.get(loader);
            PluginDescriptionFile desc = (PluginDescriptionFile) dF.get(loader);

            if (hasWatcher(lClasses)) {
                LibsDisguises.getInstance().getLogger()
                        .severe(desc.getFullName() + " has been a naughty plugin, they're declaring access to the disguise watchers before Lib's " +
                                "Disguises can properly load them! They should add 'LibsDisguises' to the 'depend' section of" + " their plugin.yml!");
                break;
            }
        }

        Field cM = JavaPluginLoader.class.getDeclaredField("classes");
        cM.setAccessible(true);
        Map<String, Class<?>> classes = (Map<String, Class<?>>) cM.get(javaLoader);

        if (hasWatcher(classes)) {
            LibsDisguises.getInstance().getLogger()
                    .severe("Somehow the main server has a Watcher instance! Hopefully there was a plugin mentioned " + "above! This is a bug!");
        }
    }

    private static boolean hasWatcher(Map<String, Class<?>> classes) {
        for (Class c : classes.values()) {
            if (!c.getName().startsWith("me.libraryaddict.disguise.disguisetypes.watchers.") &&
                    !c.getName().equals("me.libraryaddict.disguise.disguisetypes.FlagWatcher")) {
                continue;
            }

            return true;
        }

        return false;
    }

    public static void init() {
        try {
            checkPreLoaded();
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }

        if (Bukkit.getPluginManager().getPlugin("LibsDisguisesVersioning") != null) {
            throw new IllegalStateException("Why is LibsDisguisesVersioning already active? Did the server owner do something.. Weird?");
        }

        LibsDisguises.getInstance().getLogger().info("Due to issues with Java 16, you may notice harmless errors saying plugin loaded another plugin that isnt a soft depend or so on");

        FakePluginCreator fakePluginCreator = new FakePluginCreator();

        String ourVers = fakePluginCreator.getOurVersion();

        try {
            if (ourVers != null && ourVers.equals(fakePluginCreator.getVersion()) && !ourVers.contains(" CUSTOM")) {
                loadPlugin(fakePluginCreator);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        LibsDisguises.getInstance().getLogger().info("Creating a new version compatibility jar");

        ArrayList<String> mapped = new ArrayList<>();

        try (InputStream stream = LibsDisguises.getInstance().getResource("ANTI_PIRACY_ENCRYPTION")) {
            AsmLoader loader = new AsmLoader();

            Object obj;
            Method getBytes;

            if (!loader.isAsmExists()) {
                loader.doDownloadIfRequired();
                loader.loadClassloader();

                obj = Class.forName("me.libraryaddict.disguise.utilities.reflection.asm.Asm13", true, loader.getClassLoader()).newInstance();
            } else {
                obj = new Asm13();
            }

            getBytes = obj.getClass().getMethod("createClassWithoutMethods", String.class, ArrayList.class);

            String[] lines = new String(ReflectionManager.readFuzzyFully(stream), StandardCharsets.UTF_8).split("\n");

            LinkedHashMap<String, ArrayList<Map.Entry<String, String>>> toRemove = new LinkedHashMap<>();

            for (String s : lines) {
                WatcherInfo info = new WatcherInfo(s);

                if (info.isSupported()) {
                    continue;
                }

                String path = "me.libraryaddict.disguise.disguisetypes." + (info.getWatcher().equals("FlagWatcher") ? "" : "watchers.") + info.getWatcher();

                toRemove.putIfAbsent(path, new ArrayList<>());

                ArrayList<Map.Entry<String, String>> list = toRemove.get(path);

                list.add(new HashMap.SimpleEntry(info.getMethod(), info.getDescriptor()));
            }

            Map<String, byte[]> classes = new HashMap<>();

            for (Map.Entry<String, ArrayList<Map.Entry<String, String>>> entry : toRemove.entrySet()) {
                byte[] bytes = (byte[]) getBytes.invoke(obj, entry.getKey(), entry.getValue());
                mapped.add(entry.getKey());

                classes.put(entry.getKey().replace(".", "/") + ".class", bytes);
            }

            fakePluginCreator.createJar(ourVers, classes);
            loadPlugin(fakePluginCreator);

            if (!loader.isAsmExists()) {
                loader.unload();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            LibsDisguises.getInstance().getLogger().severe("Registered: " + new Gson().toJson(mapped));
        }
    }

    private static void loadPlugin(FakePluginCreator fakePluginCreator) throws Exception {
        LibsDisguises.getInstance().getLogger().info("Starting version support plugin: LibsDisguisesVersioning");
        Method method = Class.forName("org.bukkit.plugin.PluginManager", false, WatcherSanitizer.class.getClassLoader().getParent())
                .getMethod("loadPlugin", File.class);

        Plugin plugin = (Plugin) method.invoke(Bukkit.getPluginManager(), fakePluginCreator.getDestination());

        Class pluginClassLoader = Class.forName("org.bukkit.plugin.java.PluginClassLoader");

        Field loaderField = JavaPluginLoader.class.getDeclaredField("loaders");
        loaderField.setAccessible(true);
        List loaderList = (List) loaderField.get(LibsDisguises.getInstance().getPluginLoader());

        Field pluginOwner = pluginClassLoader.getDeclaredField("plugin");
        pluginOwner.setAccessible(true);

        // Move Lib's Disguises to load its classes after the new plugin
        for (Object o : loaderList) {
            if (pluginOwner.get(o) != LibsDisguises.getInstance()) {
                continue;
            }

            loaderList.remove(o);
            loaderList.add(o);
            break;
        }
    }
}
