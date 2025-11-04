package me.libraryaddict.disguise.utilities.reflection;

import io.papermc.paper.ServerBuildInfo;
import lombok.Getter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseFiles;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassMappings {
    private static final Map<String, String> classLocations = new ConcurrentHashMap<>();
    private static String[] packages;
    private static boolean updatingCache = false;
    @Getter
    private static boolean loadedCache;
    @Getter
    private static final File mappingsFile = new File(DisguiseFiles.getInternalFolder(), "mappings_cache");

    public static String getClass(String packageHint, String className, boolean can404) {
        if (!loadedCache) {
            ClassMappings.loadMappingsCache();
        }

        String key = packageHint + "." + className;
        String location = classLocations.get(key);

        if (location != null) {
            return location;
        }

        location = can404 ? "?" : "???";

        for (String pack : getPackages()) {
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
        if (packages != null) {
            return packages;
        }

        packages = new String[]{"net.minecraft.server", "net.minecraft.server.$version$", "org.bukkit.craftbukkit.$version$",
            "org.bukkit.craftbukkit.$version$.block.data", "org.bukkit.craftbukkit.$version$.entity",
            "org.bukkit.craftbukkit.$version$.inventory", "org.bukkit.craftbukkit.$version$.util"};
        String replaceStr = "$version$";
        String version = ReflectionManager.getNmsPackage();

        // If there is no nms package, then replace the . as well so we don't have a "org.bukkit..server" package name situation
        if (version.isEmpty()) {
            replaceStr = "." + replaceStr;
        }

        for (int i = 0; i < packages.length; i++) {
            packages[i] = packages[i].replace(replaceStr, version);
        }

        return packages;
    }

    private static String getVersion() {
        String version = Bukkit.getVersion() + "\t" + LibsDisguises.getInstance().getDescription().getVersion();

        // 1.21.3 has every build with this
        if (ReflectionManager.isRunningPaper() && NmsVersion.v1_21_R2.isSupported()) {
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
        if (!loadedCache) {
            return;
        }

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
        loadedCache = true;

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

                if (parts[1].equals("???")) {
                    LibsDisguises.getInstance().getLogger().info("Mappings cache pointing to " + parts[0] + " is invalid, will rebuild.");
                    classLocations.clear();
                    return;
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