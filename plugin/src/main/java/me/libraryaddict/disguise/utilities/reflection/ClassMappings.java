package me.libraryaddict.disguise.utilities.reflection;

import me.libraryaddict.disguise.LibsDisguises;
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
        String[] s = {"net.minecraft.server.$version$", "net.minecraft.core", "net.minecraft.core.particles", "net.minecraft.nbt",
            "net.minecraft.network.chat", "net.minecraft.network.protocol.game", "net.minecraft.network.syncher", "net.minecraft.resources",
            "net.minecraft.server.level", "net.minecraft.server", "net.minecraft.server.network", "net.minecraft.sounds",
            "net.minecraft.world.damagesource", "net.minecraft.world.effect", "net.minecraft.world.entity.ambient",
            "net.minecraft.world.entity.animal.axolotl", "net.minecraft.world.entity.animal", "net.minecraft.world.entity.animal.goat",
            "net.minecraft.world.entity.animal.horse", "net.minecraft.world.entity.boss.enderdragon",
            "net.minecraft.world.entity.boss.wither", "net.minecraft.world.entity.decoration", "net.minecraft.world.entity",
            "net.minecraft.world.entity.item", "net.minecraft.world.entity.monster", "net.minecraft.world.entity.monster.hoglin",
            "net.minecraft.world.entity.monster.piglin", "net.minecraft.world.entity.npc", "net.minecraft.world.entity.player",
            "net.minecraft.world.entity.projectile", "net.minecraft.world.entity.vehicle", "net.minecraft.world.inventory",
            "net.minecraft.world.item", "net.minecraft.world.level.block", "net.minecraft.world.level.block.state",
            "net.minecraft.world.level", "net.minecraft.world.phys", "org.bukkit.craftbukkit.$version$.block.data",
            "org.bukkit.craftbukkit.$version$", "org.bukkit.craftbukkit.$version$.entity", "org.bukkit.craftbukkit.$version$.inventory",
            "org.bukkit.craftbukkit.$version$.util"};
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
        return "Built for: " + Bukkit.getVersion() + "\t" + LibsDisguises.getInstance().getDescription().getVersion();
    }

    public static void saveMappingsCache(File dataFolder) {
        synchronized (classLocations) {
            if (!updatingCache) {
                return;
            }

            updatingCache = false;
        }

        File mappingsCache = new File(dataFolder, "mappings_cache");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(mappingsCache))) {
            writer.write(getVersion() + "\n");

            for (Map.Entry<String, String> entry : classLocations.entrySet()) {
                writer.write(entry.getKey() + " " + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadMappingsCache(File dataFolder) {
        File mappingsCache = new File(dataFolder, "mappings_cache");
        if (!mappingsCache.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(mappingsCache))) {
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