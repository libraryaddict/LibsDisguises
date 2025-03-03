package me.libraryaddict.disguise.utilities.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.config.migrations.ConfigMigration_DisabledMethods;
import me.libraryaddict.disguise.utilities.config.migrations.ConfigMigration_DisguiseScaling;
import me.libraryaddict.disguise.utilities.config.migrations.ConfigMigration_RenamedFiles;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigMigrator {
    public interface ConfigMigration {
        /**
         * The names of the configs that need to be loaded to import the configs
         * <p>
         * If a config name in this is missing from {@link #getFilesToMigrateTo()} then the config will be deleted afterwards
         */
        String[] getFilesToMigrateFrom();

        /**
         * The names of the configs that will be modified.
         * <p>
         * If a file used in {@link #getFilesToMigrateFrom()} is not in this array, it will be deleted
         */
        String[] getFilesToMigrateTo();

        default List<String> getAllFilesTouched() {
            List<String> list = new ArrayList<>(Arrays.asList(getFilesToMigrateFrom()));

            for (String string : getFilesToMigrateTo()) {
                if (list.stream().anyMatch(string::equals)) {
                    continue;
                }

                list.add(string);
            }

            return list;
        }

        /**
         * The version this config is
         */
        int getVersion();

        void migrate(YamlConfiguration loadedConfig);
    }

    private final Set<String> alreadyLoaded = new HashSet<>();
    private final Set<String> toDelete = new HashSet<>();
    private final Set<String> toWrite = new HashSet<>();

    private static List<ConfigMigration> getMigrations() {
        List<ConfigMigration> list = new ArrayList<>();

        // V.1
        list.add(new ConfigMigration_RenamedFiles());
        // In newer builds, these are added
        list.add(new ConfigMigration_DisabledMethods(1, "setNameYModifier", "setInvisible", "setUnsafeSize", "setScalePlayerToDisguise"));
        // V.2
        list.add(new ConfigMigration_DisguiseScaling());

        return list;
    }

    public static int getLastMigrationVersion() {
        return getMigrations().stream().mapToInt(ConfigMigration::getVersion).max().orElse(-1);
    }

    public void runMigrations() {
        List<ConfigMigration> migrations = getMigrations();
        migrations.removeIf(m -> m.getVersion() <= DisguiseConfig.getLastConfigVersion());

        if (migrations.isEmpty()) {
            return;
        }

        int version = DisguiseConfig.getLastConfigVersion();
        int finalVersion = migrations.stream().map(ConfigMigration::getVersion).max(Integer::compareTo).get();
        LibsDisguises.getInstance().getLogger()
            .info("Running " + migrations.size() + " config migrations.. Jumping from version " + version + " to " + finalVersion);
        YamlConfiguration globalConfig = new YamlConfiguration();

        // Each migration assumes its the last migration, may not be optimal but its less confusing and unnotable in the end
        for (ConfigMigration migration : migrations) {
            runMigration(migration, globalConfig);
            version = Math.max(version, migration.getVersion());
        }

        // Save!
        for (String filename : toWrite) {
            LibsDisguises.getInstance().getLogger().info("Saving config " + filename);
            ConfigLoader.saveConfig(globalConfig, filename);
        }

        for (String oldFile : toDelete) {
            File file = getConfig(oldFile);

            if (!file.exists()) {
                continue;
            }

            file.delete();
            LibsDisguises.getInstance().getLogger().info("Removing legacy config file " + oldFile);
        }

        DisguiseConfig.setLastConfigVersion(version);
        DisguiseConfig.saveInternalConfig();
    }

    private void runMigration(ConfigMigration migration, YamlConfiguration globalConfig) {
        for (String filename : migration.getAllFilesTouched()) {
            addConfigToConfig(filename, globalConfig);
        }

        migration.migrate(globalConfig);

        // Add files that dont exist in new versions to be deleted
        for (String migrateFrom : migration.getFilesToMigrateFrom()) {
            // If file doesnt exist in "kept files"
            if (Arrays.asList(migration.getFilesToMigrateTo()).contains(migrateFrom)) {
                continue;
            }

            toDelete.add(migrateFrom);
            toWrite.remove(migrateFrom);
        }

        // Remove files that exist in new version, might have been added by previous migration!
        for (String migrateTo : migration.getFilesToMigrateTo()) {
            toDelete.remove(migrateTo);
            toWrite.add(migrateTo);
        }
    }

    private File getConfig(String name) {
        return new File(LibsDisguises.getInstance().getDataFolder(), "configs/" + name);
    }

    private void addConfigToConfig(String filename, YamlConfiguration globalConfig) {
        if (alreadyLoaded.contains(filename)) {
            return;
        }

        alreadyLoaded.add(filename);

        File file = getConfig(filename);

        if (!file.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String k : config.getKeys(true)) {
            if (config.isConfigurationSection(k)) {
                continue;
            }

            if (globalConfig.isConfigurationSection(k) || globalConfig.isSet(k)) {
                continue;
            }

            globalConfig.set(k, config.get(k));
        }
    }
}
