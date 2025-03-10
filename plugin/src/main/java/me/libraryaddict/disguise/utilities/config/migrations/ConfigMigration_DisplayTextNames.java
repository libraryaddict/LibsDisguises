package me.libraryaddict.disguise.utilities.config.migrations;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.config.ConfigMigrator;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigMigration_DisplayTextNames implements ConfigMigrator.ConfigMigration {
    @Override
    public String[] getFilesToMigrateFrom() {
        return new String[]{"displays.yml"};
    }

    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public void migrate(YamlConfiguration loadedConfig) {
        // Empty, used to update config comments
    }
}
