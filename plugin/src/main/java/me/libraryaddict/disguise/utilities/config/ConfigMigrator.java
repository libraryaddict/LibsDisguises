package me.libraryaddict.disguise.utilities.config;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.config.migrations.ConfigMigration_DisabledMethods;
import me.libraryaddict.disguise.utilities.config.migrations.ConfigMigration_DisguiseScaling;
import me.libraryaddict.disguise.utilities.config.migrations.ConfigMigration_DisplayConfigDesc;
import me.libraryaddict.disguise.utilities.config.migrations.ConfigMigration_DisplayTextNames;
import me.libraryaddict.disguise.utilities.config.migrations.ConfigMigration_RenamedFiles;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
        default String[] getFilesToMigrateTo() {
            // Defaulted to providing the "migrate from" to avoid accidental mistakes!
            return getFilesToMigrateFrom();
        }

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

        /**
         * Programatically detect if this migration can be applied again, used to correct failed migrations
         */
        default boolean isRelevant() {
            return false;
        }
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
        // V.3
        list.add(new ConfigMigration_DisplayTextNames());
        // V.4
        list.add(new ConfigMigration_DisplayConfigDesc());

        return list;
    }

    public static int getLastMigrationVersion() {
        return getMigrations().stream().mapToInt(ConfigMigration::getVersion).max().orElse(-1);
    }

    public void runMigrations() {
        List<ConfigMigration> migrations = getMigrations();
        int currentMigration = DisguiseConfig.getLastConfigVersion();

        Iterator<ConfigMigration> iterator = migrations.iterator();

        while (iterator.hasNext()) {
            ConfigMigration migration = iterator.next();

            // If this migration will be applied, continue as it will not be removed
            if (migration.getVersion() > currentMigration) {
                continue;
            }

            // If this migration wasn't applied properly in the past, as determined programatically
            // Set the current migration level, and continue without removing
            if (migration.isRelevant()) {
                currentMigration = Math.min(currentMigration, migration.getVersion() - 1);
                LibsDisguises.getInstance().getLogger()
                    .info("Old config migration doesn't seem to have applied properly, will try migrating configs again.");
                continue;
            }

            // This migration is older than the current migration we will be applying
            iterator.remove();
        }

        if (migrations.isEmpty()) {
            return;
        }

        int version = currentMigration;
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
