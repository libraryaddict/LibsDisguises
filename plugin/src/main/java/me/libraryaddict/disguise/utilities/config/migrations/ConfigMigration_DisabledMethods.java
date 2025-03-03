package me.libraryaddict.disguise.utilities.config.migrations;

import java.util.ArrayList;
import java.util.List;

import me.libraryaddict.disguise.utilities.config.ConfigMigrator;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigMigration_DisabledMethods implements ConfigMigrator.ConfigMigration {
    private final String[] defaultDisabledMethods;
    private final int version;

    public ConfigMigration_DisabledMethods(int version, String... addedMethods) {
        this.version = version;
        defaultDisabledMethods = addedMethods;
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
    public void migrate(YamlConfiguration globalConfig) {
        // If not set to a valid value, unset it and let us revert it to defaults
        if (!globalConfig.isSet("DisabledMethods") || !globalConfig.isList("DisabledMethods")) {
            globalConfig.set("DisabledMethods", null);
            return;
        }

        List<String> list = globalConfig.getStringList("DisabledMethods");
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

        globalConfig.set("DisabledMethods", toAdd);
    }
}
