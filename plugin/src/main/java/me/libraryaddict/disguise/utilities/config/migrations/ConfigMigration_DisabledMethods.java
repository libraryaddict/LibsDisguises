package me.libraryaddict.disguise.utilities.config.migrations;

import me.libraryaddict.disguise.utilities.config.ConfigMigrator;
import me.libraryaddict.disguise.utilities.config.SharedYamlConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigMigration_DisabledMethods implements ConfigMigrator.ConfigMigration {
    private final List<String> defaultDisabledMethods;
    private final int version;

    public ConfigMigration_DisabledMethods(int version, String... addedMethods) {
        this.version = version;
        defaultDisabledMethods = Arrays.asList(addedMethods);
    }

    @Override
    public String[] getFilesToMigrateFrom() {
        return new String[]{"self_disguise.yml"};
    }

    @Override
    public String[] getFilesToMigrateTo() {
        return new String[]{"self_disguise.yml"};
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public void migrate(SharedYamlConfiguration migrateFrom, YamlConfiguration migrateTo) {
        // If not set to a valid value, unset it and let us revert it to defaults
        if (!migrateTo.isSet("DisabledMethods") || !migrateTo.isList("DisabledMethods")) {
            migrateTo.set("DisabledMethods", null);
            return;
        }

        List<String> list = migrateTo.getStringList("DisabledMethods");
        List<String> toAdd = new ArrayList<>(list);

        for (String setting : defaultDisabledMethods) {
            if (toAdd.stream().anyMatch(s -> s.equalsIgnoreCase(setting))) {
                continue;
            }

            toAdd.add(setting);
        }

        if (toAdd.size() == list.size()) {
            return;
        }

        migrateTo.set("DisabledMethods", toAdd);
    }
}
