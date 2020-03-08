package me.libraryaddict.disguise.utilities.reflection.asm;

import com.google.gson.Gson;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
                LibsDisguises.getInstance().getLogger().severe(desc.getFullName() +
                        " has been a naughty plugin, they're declaring access to the disguise watchers before Lib's " +
                        "Disguises can properly load them! They should add 'LibsDisguises' to the 'depend' section of" +
                        " their plugin.yml!");
                break;
            }
        }

        Field cM = JavaPluginLoader.class.getDeclaredField("classes");
        cM.setAccessible(true);
        Map<String, Class<?>> classes = (Map<String, Class<?>>) cM.get(javaLoader);

        if (hasWatcher(classes)) {
            LibsDisguises.getInstance().getLogger()
                    .severe("Somehow the main server has a Watcher instance! Hopefully there was a plugin mentioned " +
                            "above! This is a bug!");
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
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        ArrayList<String> mapped = new ArrayList<>();

        try (InputStream stream = LibsDisguises.getInstance().getResource("ANTI_PIRACY_ENCRYPTION")) {
            IAsm asm;

            if (NmsVersion.v1_14.isSupported()) {
                asm = new Asm14();
            } else {
                if (!NmsVersion.v1_13.isSupported()) {
                    new AsmDownloader();
                }

                asm = new Asm13();
            }

            List<String> lines = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines()
                    .collect(Collectors.toList());

            LinkedHashMap<String, ArrayList<Map.Entry<String, String>>> toRemove = new LinkedHashMap<>();

            for (String s : lines) {
                WatcherInfo info = new WatcherInfo(s);

                if (info.isSupported()) {
                    continue;
                }

                String path = "me.libraryaddict.disguise.disguisetypes." +
                        (info.getWatcher().equals("FlagWatcher") ? "" : "watchers.") + info.getWatcher();

                toRemove.putIfAbsent(path, new ArrayList<>());

                ArrayList<Map.Entry<String, String>> list = toRemove.get(path);

                list.add(new HashMap.SimpleEntry(info.getMethod(), info.getDescriptor()));
            }

            for (Map.Entry<String, ArrayList<Map.Entry<String, String>>> entry : toRemove.entrySet()) {
                Class result = asm.createClassWithoutMethods(entry.getKey(), entry.getValue());
                mapped.add(entry.getKey());
            }
        }
        catch (IOException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException | LinkageError e) {
            e.printStackTrace();
            LibsDisguises.getInstance().getLogger().severe("Registered: " + new Gson().toJson(mapped));
        }
    }
}
