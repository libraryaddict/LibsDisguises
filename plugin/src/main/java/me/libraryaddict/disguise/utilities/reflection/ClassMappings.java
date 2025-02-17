package me.libraryaddict.disguise.utilities.reflection;

import io.papermc.paper.ServerBuildInfo;
import lombok.Getter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ClassMappings {
    private static final HashMap<String, String> classLocations = new HashMap<>();
    private static final String[] packages = getPackages();
    private static boolean updatingCache = false;
    @Getter
    private static final File mappingsFile = new File(DisguiseUtilities.getInternalFolder(), "mappings_cache");

    static {
        ClassMappings.loadMappingsCache();
    }

    public static String getClass(String packageHint, String className) {
        String key = packageHint + "." + className;
        String location = classLocations.get(key);

        if (location != null) {
            return location;
        }

        location = "???";

        for (String pack : packages) {
            if (!pack.startsWith(packageHint)) {
                continue;
            }

            String toTry = pack + "." + className;
            try {
                Class.forName(toTry);
                location = toTry;
                break;
            } catch (Throwable ignored) {
            }
        }

        classLocations.put(key, location);

        synchronized (classLocations) {
            if (!updatingCache && LibsDisguises.getInstance() != null && LibsDisguises.getInstance().isEnabled()) {
                // Run 10 seconds later
                updatingCache = true;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ClassMappings.saveMappingsCache(LibsDisguises.getInstance().getDataFolder());
                    }
                }.runTaskLater(LibsDisguises.getInstance(), 10 * 20);
            }
        }
        return location;
    }

    private static String[] getPackages() {
        String[] s = {"net.minecraft.server", "net.minecraft.server.$version$", "org.bukkit.craftbukkit.$version$",
            "org.bukkit.craftbukkit.$version$.block.data", "org.bukkit.craftbukkit.$version$.entity",
            "org.bukkit.craftbukkit.$version$.inventory", "org.bukkit.craftbukkit.$version$.util"};
        String replaceStr = "$version$";
        String version = ReflectionManager.getNmsPackage();

        // If there is no nms package, then replace the . as well so we don't have a "org.bukkit..server" package name situation
        if (version.isEmpty()) {
            replaceStr = "." + replaceStr;
        }

        for (int i = 0; i < s.length; i++) {
            s[i] = s[i].replace(replaceStr, version);
        }

        return s;
    }

    private static String getVersion() {
        String version = Bukkit.getVersion() + "\t" + LibsDisguises.getInstance().getDescription().getVersion();

        // 1.21.3 has every build with this
        if (DisguiseUtilities.isRunningPaper() && NmsVersion.v1_21_R2.isSupported()) {
            ServerBuildInfo buildInfo = ServerBuildInfo.buildInfo();

            if (buildInfo != null && buildInfo.buildTime() != null) {
                version += "\t" + buildInfo.buildTime();
            }
        }

        return "Built for: " + version;
    }

    public static void deleteMappingsCache() {
        getMappingsFile().delete();
    }

    public static void saveMappingsCache(File dataFolder) {
        synchronized (classLocations) {
            if (!updatingCache) {
                return;
            }

            updatingCache = false;
        }

        getMappingsFile().getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getMappingsFile()))) {
            writer.write(getVersion() + "\n");

            for (Map.Entry<String, String> entry : classLocations.entrySet()) {
                writer.write(entry.getKey() + " " + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadMappingsCache() {
        if (!getMappingsFile().exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(getMappingsFile()))) {
            String line = reader.readLine();

            // Not the correct version
            if (line == null || !line.equals(getVersion())) {
                LibsDisguises.getInstance().getLogger().info("Outdated mappings cache, will rebuild.");
                return;
            }

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 2);

                if (parts.length != 2) {
                    continue;
                }

                try {
                    // Check if class name is still valid
                    if (parts[1].contains(".")) {
                        Class.forName(parts[1]);
                    }

                    classLocations.put(parts[0], parts[1]);
                } catch (ClassNotFoundException e) {
                    // silently discard, something went wrong though
                }
            }
        } catch (FileNotFoundException e) {
            // silently discard, it doesn't matter if the cache doesn't exist, we will just create it later
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}