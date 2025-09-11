package me.libraryaddict.disguise.utilities;

import lombok.Getter;
import lombok.SneakyThrows;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class DisguiseFiles {
    @Getter
    private static final File internalFolder, preferencesFile, profileCache, sanitySkinCacheFile, savedDisguises;

    static {
        if (LibsDisguises.getInstance() == null) {
            profileCache = null;
            sanitySkinCacheFile = null;
            savedDisguises = null;
            internalFolder = null;
            preferencesFile = null;
        } else {
            profileCache = new File(LibsDisguises.getInstance().getDataFolder(), "SavedSkins");
            sanitySkinCacheFile = new File(LibsDisguises.getInstance().getDataFolder(), "SavedSkins/sanity.json");
            savedDisguises = new File(LibsDisguises.getInstance().getDataFolder(), "SavedDisguises");
            internalFolder = new File(LibsDisguises.getInstance().getDataFolder(), "internal");
            preferencesFile = new File(getInternalFolder(), "preferences.json");
        }

        init();
    }

    public static void init() {
        if (getInternalFolder() == null) {
            return;
        }

        // Ensure /internal exists

        if (!getInternalFolder().exists()) {
            getInternalFolder().mkdirs();
        }
    }

    public static String getResourceAsString(File file, String fileName) {
        try {
            return getResourceAsStringEx(file, fileName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static int getJarFileCount(File file) throws IOException {
        try (JarFile jar = new JarFile(file)) {
            int count = 0;

            Enumeration<JarEntry> entry = jar.entries();

            while (entry.hasMoreElements()) {
                if (entry.nextElement().isDirectory()) {
                    continue;
                }

                count++;
            }

            return count;
        }
    }

    @SneakyThrows
    public static String getResourceAsStringEx(File file, String fileName) {
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry(fileName);

            try (InputStream stream = jar.getInputStream(entry)) {
                return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            }
        }
    }

    public static List<File> getFilesByPlugin(String pluginName) {
        return getFilesByPlugin(LibsDisguises.getInstance().getDataFolder().getAbsoluteFile().getParentFile(), pluginName);
    }

    public static List<File> getFilesByPlugin(File containingFolder, String pluginName) {
        List<File> files = new ArrayList<>();

        for (File file : containingFolder.listFiles()) {
            if (!file.isFile() || !file.getName().toLowerCase(Locale.ENGLISH).endsWith(".jar")) {
                continue;
            }

            YamlConfiguration config = null;

            try {
                config = getPluginYAMLEx(file);
            } catch (Throwable ex) {
                if (DisguiseConfig.isVerboseLogging()) {
                    ex.printStackTrace();
                }
            }

            if (config == null) {
                continue;
            }

            // If not the right plugin
            if (!pluginName.equalsIgnoreCase(config.getString("name"))) {
                continue;
            }

            files.add(file);
        }

        return files;
    }

    /**
     * Copied from Bukkit
     */
    public static YamlConfiguration getPluginYAML(File file) {
        try {
            return getPluginYAMLEx(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static YamlConfiguration getPluginYAMLEx(File file) throws Exception {
        String s = getResourceAsString(file, "plugin.yml");

        if (s == null) {
            return null;
        }

        YamlConfiguration config = new YamlConfiguration();

        config.loadFromString(getResourceAsString(file, "plugin.yml"));

        return config;
    }
}
