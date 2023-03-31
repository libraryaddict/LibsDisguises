package me.libraryaddict.disguise.utilities.config;

import lombok.Getter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.ClassGetter;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by libraryaddict on 31/01/2021.
 */
public class ConfigLoader {
    @Getter
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
    }

    public void saveMissingConfigs() {
        File oldFile = new File(LibsDisguises.getInstance().getDataFolder(), "config.yml");
        boolean migrated = oldFile.exists();

        for (String config : configs) {
            File f = new File(LibsDisguises.getInstance().getDataFolder(), config);

            if (f.exists()) {
                migrated = false;
                continue;
            }

            saveDefaultConfig(config);
        }

        if (migrated) {
            DisguiseUtilities.getLogger().info("Migrated old config system to new config system");
            oldFile.delete();
        }
    }

    public YamlConfiguration loadDefaults() {
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

    public YamlConfiguration load() {
        YamlConfiguration globalConfig = new YamlConfiguration();

        for (String config : configs) {
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

    public void saveDefaultConfigs() {
        for (String config : configs) {
            saveDefaultConfig(config);
        }

        File f = new File(LibsDisguises.getInstance().getDataFolder(), "config.yml");

        f.delete();
    }

    public void saveDefaultConfig(String name) {
        DisguiseUtilities.getLogger().info("Config " + name + " is out of date (Or missing)! Now refreshing it!");
        String ourConfig = ReflectionManager.getResourceAsString(LibsDisguises.getInstance().getFile(), name);
        YamlConfiguration savedConfig = null;

        File loadFrom = new File(LibsDisguises.getInstance().getDataFolder(), name);
        File configFile = loadFrom;

        if (!loadFrom.exists()) {
            loadFrom = new File(LibsDisguises.getInstance().getDataFolder(), "config.yml");
        }

        if (loadFrom.exists()) {
            savedConfig = YamlConfiguration.loadConfiguration(loadFrom);
        } else {
            try {
                savedConfig = new YamlConfiguration();
                savedConfig.loadFromString(ourConfig);
            } catch (Exception e) {
                e.printStackTrace();
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
