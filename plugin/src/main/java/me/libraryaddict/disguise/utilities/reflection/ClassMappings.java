package me.libraryaddict.disguise.utilities.reflection;

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

    public static String getClass(String packageHint, String className) {
        String location = classLocations.get(className);
        if (location != null) {
            return location;
        }
        location = className;
        String[] arrayOfString;
        int i;
        byte b;
        for (arrayOfString = packages, i = arrayOfString.length, b = 0; b < i; ) {
            String pack = arrayOfString[b];
            if (!pack.startsWith(packageHint)) {
                b++;
                continue;
            }
            String toTry = pack + "." + className;
            try {
                Class.forName(toTry);
                location = pack + "." + className;
                break;
            } catch (Throwable throwable) {
                b++;
            }
        }
        classLocations.put(className, location);
        return location;
    }

    private static String[] getPackages() {
        String[] s = {"net.minecraft.server.$version$", "net.minecraft.core", "net.minecraft.core.particles", "net.minecraft.nbt", "net.minecraft.network.chat",
            "net.minecraft.network.protocol.game", "net.minecraft.network.syncher", "net.minecraft.resources", "net.minecraft.server.level",
            "net.minecraft.server", "net.minecraft.server.network", "net.minecraft.sounds", "net.minecraft.world.damagesource", "net.minecraft.world.effect",
            "net.minecraft.world.entity.ambient", "net.minecraft.world.entity.animal.axolotl", "net.minecraft.world.entity.animal",
            "net.minecraft.world.entity.animal.goat", "net.minecraft.world.entity.animal.horse", "net.minecraft.world.entity.boss.enderdragon",
            "net.minecraft.world.entity.boss.wither", "net.minecraft.world.entity.decoration", "net.minecraft.world.entity", "net.minecraft.world.entity.item",
            "net.minecraft.world.entity.monster", "net.minecraft.world.entity.monster.hoglin", "net.minecraft.world.entity.monster.piglin",
            "net.minecraft.world.entity.npc", "net.minecraft.world.entity.player", "net.minecraft.world.entity.projectile",
            "net.minecraft.world.entity.vehicle", "net.minecraft.world.inventory", "net.minecraft.world.item", "net.minecraft.world.level.block",
            "net.minecraft.world.level.block.state", "net.minecraft.world.level", "net.minecraft.world.phys", "org.bukkit.craftbukkit.$version$.block.data",
            "org.bukkit.craftbukkit.$version$", "org.bukkit.craftbukkit.$version$.entity", "org.bukkit.craftbukkit.$version$.inventory",
            "org.bukkit.craftbukkit.$version$.util"};
        for (int i = 0; i < s.length; i++) {
            s[i] = s[i].replace("$version$", ReflectionManager.getBukkitVersion());
        }
        return s;
    }

    public static void saveMappingsCache(File dataFolder) {
        File mappingsCache = new File(dataFolder, "mappings_cache");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(mappingsCache))) {
            for (Map.Entry<String, String> entry : classLocations.entrySet()) {
                if (!entry.getKey().equals(entry.getValue())) {
                    writer.write(entry.getKey() + " " + entry.getValue() + "\n");
                }
            }
        } catch (IOException e) {
            // don't care if cache can't be saved
            e.printStackTrace();
        }
    }

    public static void loadMappingsCache(File dataFolder) {
        File mappingsCache = new File(dataFolder, "mappings_cache");
        try (BufferedReader reader = new BufferedReader(new FileReader(mappingsCache))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 2);
                if (parts.length == 2) {
                    // Check if class name is still valid
                    try {
                        Class.forName(parts[1]);
                        classLocations.put(parts[0], parts[1]);
                    } catch (ClassNotFoundException e) {
                        // silently discard, we may have just changed versions
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // silently discard, it doesn't matter if the cache doesn't exist, we will just create it later
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}