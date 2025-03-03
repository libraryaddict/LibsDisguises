package me.libraryaddict.disguise.utilities.config.migrations;

import me.libraryaddict.disguise.utilities.config.ConfigMigrator;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigMigration_RenamedFiles implements ConfigMigrator.ConfigMigration {
    /**
     * Configs that existed at the time
     */
    private final String[] allOldFiles =
        new String[]{"combat.yml", "features.yml", "libsdisguises.yml", "nametags.yml", "players.yml", "protocol.yml", "sanity.yml"};
    /**
     * Configs that now exist
     */
    private final String[] allCurrentFiles =
        new String[]{"dangerous.yml", "displays.yml", "events.yml", "libsdisguises.yml", "premium.yml", "protocol.yml",
            "self_disguise.yml"};

    @Override
    public String[] getFilesToMigrateFrom() {
        return allOldFiles;
    }

    @Override
    public String[] getFilesToMigrateTo() {
        return allCurrentFiles;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void migrate(YamlConfiguration migrateTo) {
        // Mostly files being shuffled around
    }
}
