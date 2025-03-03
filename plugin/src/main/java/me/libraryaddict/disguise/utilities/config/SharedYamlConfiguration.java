package me.libraryaddict.disguise.utilities.config;

import lombok.Getter;
import me.libraryaddict.disguise.LibsDisguises;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SharedYamlConfiguration {
    private final Map<String, YamlConfiguration> configs = new HashMap<>();
    private final Map<String, String> configLocations = new HashMap<>();
    @Getter
    private final YamlConfiguration globalConfig = new YamlConfiguration();
    private final Set<String> configsChanged = new HashSet<>();

    /**
     * Adds the config to the global config
     */
    public void addConfig(String configName, YamlConfiguration config) {
        configs.put(configName, config);

        for (String k : config.getKeys(true)) {
            if (config.isConfigurationSection(k)) {
                continue;
            }

            if (globalConfig.contains(k)) {
 //               LibsDisguises.getInstance().getLogger().warning("Duplicate key '" + k + "' found in " + configName);
            }

            globalConfig.set(k, config.get(k));
            configLocations.put(k, configName);
        }
    }

    /**
     * Returns the name of the file that had this config key
     */
    public String getOwningConfig(String configKey) {
        return configLocations.get(configKey);
    }

    /**
     * Returns the config by the file name
     */
    public YamlConfiguration getConfig(String key) {
        return configs.get(key);
    }

    /**
     * Sets the config as changed and returns it
     */
    public YamlConfiguration getConfigToChange(String key) {
        setChanged(key);

        return getConfig(key);
    }

    /**
     * If this config is marked dirty and needs to be saved
     */
    public boolean isChanged(String config) {
        return configsChanged.contains(config);
    }

    /**
     * Mark the config dirty and needing saving
     */
    public void setChanged(String config) {
        configsChanged.add(config);
    }
}
