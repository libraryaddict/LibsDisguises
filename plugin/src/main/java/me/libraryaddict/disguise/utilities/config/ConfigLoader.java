package me.libraryaddict.disguise.utilities.config;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.reflection.ClassGetter;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

@Getter
public class ConfigLoader {
    private final List<String> configs = new ArrayList<>();

    public ConfigLoader() {
        for (String s : ClassGetter.getEntriesForPackage(ConfigLoader.class, "configs")) {
            if (!s.endsWith(".yml")) {
                continue;
            }

            if (s.endsWith("/disguises.yml") || s.endsWith("/sounds.yml")) {
                continue;
            }

            configs.add(s);
        }

        new ConfigMigrator().runMigrations();
    }

    public void saveMissingConfigs() {
        for (String config : configs) {
            File f = new File(LibsDisguises.getInstance().getDataFolder(), config);

            if (f.exists()) {
                continue;
            }

            saveDefaultConfig(config);
        }
    }

    private YamlConfiguration loadDefaults(List<String> configs) {
        YamlConfiguration globalConfig = new YamlConfiguration();

        for (String config : configs) {
            try {
                YamlConfiguration c = new YamlConfiguration();
                c.loadFromString(ReflectionManager.getResourceAsString(LibsDisguises.getInstance().getFile(), config));

                for (String k : c.getKeys(true)) {
                    if (c.isConfigurationSection(k)) {
                        continue;
                    }

                    globalConfig.set(k, c.get(k));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return globalConfig;
    }

    public YamlConfiguration loadDefaults() {
        return loadDefaults(configs);
    }

    public YamlConfiguration load() {
        return load(configs);
    }

    public YamlConfiguration load(List<String> configNames) {
        YamlConfiguration globalConfig = new YamlConfiguration();

        for (String config : configNames) {
            YamlConfiguration c = YamlConfiguration.loadConfiguration(new File(LibsDisguises.getInstance().getDataFolder(), config));

            for (String k : c.getKeys(true)) {
                if (c.isConfigurationSection(k)) {
                    continue;
                }

                globalConfig.set(k, c.get(k));
            }
        }

        return globalConfig;
    }

    public boolean isOutdated(String config) {
        if (!config.startsWith("configs/")) {
            config = "configs/" + config;
        }

        File file = new File(LibsDisguises.getInstance().getDataFolder(), config);

        if (!file.exists()) {
            return true;
        }

        try {
            YamlConfiguration savedConfig = YamlConfiguration.loadConfiguration(file);

            String ourConfig = ReflectionManager.getResourceAsString(LibsDisguises.getInstance().getFile(), config);
            YamlConfiguration internalConfig = new YamlConfiguration();
            internalConfig.loadFromString(ourConfig);

            // Loop over all the keys
            for (String key : internalConfig.getKeys(true)) {
                // Skip section identifiers
                if (internalConfig.isConfigurationSection(key)) {
                    continue;
                }

                // If the saved config has this key, continue
                if (savedConfig.isSet(key)) {
                    continue;
                }

                // Key is missing, config is not default. Return true.
                return true;
            }

            // Return false, saved config is default
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    public void saveDefaultConfigs() {
        for (String config : configs) {
            if (!isOutdated(config)) {
                continue;
            }

            saveDefaultConfig(config);
        }
    }

    public void saveDefaultConfig(String configName) {
        File loadFrom = new File(LibsDisguises.getInstance().getDataFolder(), configName);

        YamlConfiguration savedConfig = null;

        String neatName = configName.replace("configs/", "");

        if (loadFrom.exists()) {
            LibsDisguises.getInstance().getLogger().info("Updating config " + neatName + " with missing entries.");
            savedConfig = YamlConfiguration.loadConfiguration(loadFrom);
        } else {
            LibsDisguises.getInstance().getLogger().info("Saving default config " + neatName);
        }

        saveConfig(savedConfig, configName);
    }

    static void saveConfig(YamlConfiguration savedConfig, String configName) {
        if (!configName.startsWith("configs/")) {
            configName = "configs/" + configName;
        }

        String ourConfig = ReflectionManager.getResourceAsString(LibsDisguises.getInstance().getFile(), configName);

        if (savedConfig == null) {
            try {
                savedConfig = new YamlConfiguration();
                savedConfig.loadFromString(ourConfig);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        List<String> strings = new ArrayList<>();
        String[] string = ourConfig.split("\n");
        boolean loadingList = false;
        boolean useInternalList = false;
        StringBuilder section = new StringBuilder();

        for (String s : string) {
            if (s.trim().startsWith("-") && loadingList) {
                if (useInternalList) {
                    strings.add(s);
                }

                continue;
            } else if (loadingList) {
                loadingList = false;
            }

            if (s.trim().startsWith("#") || !s.contains(":") || s.trim().startsWith("-")) {
                strings.add(s);
                continue;
            }

            String rawKey = s.split(":")[0];

            if (section.length() > 0) {
                int matches = StringUtils.countMatches(rawKey, "  ");

                int allowed = 0;

                for (int a = 0; a < matches; a++) {
                    allowed = section.indexOf(".", allowed) + 1;
                }

                section = new StringBuilder(section.substring(0, allowed));
            }

            String key = (rawKey.startsWith(" ") ? section.toString() : "") + rawKey.trim();

            if (savedConfig.isConfigurationSection(key)) {
                section.append(key).append(".");
            } else if (savedConfig.isList(key)) {
                strings.add(s);
                loadingList = true;
                useInternalList = !savedConfig.isSet(key);

                if (!useInternalList) {
                    for (String a : savedConfig.getStringList(key)) {
                        strings.add(rawKey.substring(0, Math.max(0, rawKey.indexOf(" ") + 1)) + "  - " + a);
                    }
                }

                continue;
            } else if (savedConfig.isSet(key)) {
                String rawVal = s.split(":")[1].trim();
                Object val = savedConfig.get(key);

                if (savedConfig.isString(key) && !rawVal.equals("true") && !rawVal.equals("false")) {
                    val = "'" + StringEscapeUtils.escapeJava(val.toString().replace(ChatColor.COLOR_CHAR + "", "&")) + "'";
                }

                s = rawKey + ": " + val;
            }

            strings.add(s);
        }

        File configFile = new File(LibsDisguises.getInstance().getDataFolder(), configName);

        try {
            if (!configFile.getParentFile().exists()) {
                configFile.mkdirs();
            }

            configFile.delete();
            configFile.createNewFile();

            try (PrintWriter out = new PrintWriter(configFile)) {
                out.write(String.join("\n", strings));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
