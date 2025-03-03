package me.libraryaddict.disguise.utilities.config.migrations;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.utilities.config.ConfigMigrator;
import me.libraryaddict.disguise.utilities.config.SharedYamlConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Locale;

public class ConfigMigration_DisguiseScaling implements ConfigMigrator.ConfigMigration {
    @Override
    public String[] getFilesToMigrateFrom() {
        return new String[]{"dangerous.yml"};
    }

    @Override
    public String[] getFilesToMigrateTo() {
        return new String[]{"dangerous.yml"};
    }

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public void migrate(SharedYamlConfiguration migrateFrom, YamlConfiguration keptValues) {
        // We only need to handle config settings that 'changed'

        // TallSelfDisguises
        DisguiseConfig.TallSelfDisguise tallSelfDisguisesVisibility = getVisibility(keptValues);

        // If its not set in the new config
        if (tallSelfDisguisesVisibility == null) {
            // Make sure it doesn't exist in the old config
            tallSelfDisguisesVisibility = getVisibility(migrateFrom.getGlobalConfig());

            // Try resolve it from booleans
            if (tallSelfDisguisesVisibility == null) {
                // Previously, 'true' means they want tall disguises to obscure the player's view
                if (migrateFrom.getGlobalConfig().getBoolean("TallSelfDisguises")) {
                    tallSelfDisguisesVisibility = DisguiseConfig.TallSelfDisguise.VISIBLE;
                    // Previously, 'true' meant that if the above was false, they want the disguise to be scaled down
                } else if (migrateFrom.getGlobalConfig().getBoolean("TallSelfDisguisesScaling")) {
                    tallSelfDisguisesVisibility = DisguiseConfig.TallSelfDisguise.SCALED;
                    // And if tall disguises are disabled and scaling is disabled, they don't want tall disguises to show at all
                } else {
                    tallSelfDisguisesVisibility = DisguiseConfig.TallSelfDisguise.HIDDEN;
                }
            }

            keptValues.set("TallSelfDisguises", tallSelfDisguisesVisibility);
        }

        // TODO Changes that may have been done over the dev build period
        // TODO Add the list array

    }

    private DisguiseConfig.TallSelfDisguise getVisibility(YamlConfiguration configuration) {
        try {
            return DisguiseConfig.TallSelfDisguise.valueOf(configuration.getString("TallSelfDisguises").toUpperCase(Locale.ENGLISH));
        } catch (Exception ignored) {
        }
        return null;
    }
}
