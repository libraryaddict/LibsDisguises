package me.libraryaddict.disguise.utilities.config.migrations;

import java.util.Locale;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.utilities.config.ConfigMigrator;
import org.bukkit.configuration.file.YamlConfiguration;

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
    public void migrate(YamlConfiguration globalConfig) {
        // Let defaults work itself out if this wasn't set
        if (!globalConfig.isSet("TallSelfDisguises")) {
            return;
        }

        // We only need to handle config settings that 'changed'

        // TallSelfDisguises
        DisguiseConfig.TallSelfDisguise tallSelfDisguisesVisibility = getVisibility(globalConfig);

        // If its not set in the new config
        if (tallSelfDisguisesVisibility == null) {
            // Make sure it doesn't exist in the old config
            tallSelfDisguisesVisibility = getVisibility(globalConfig);

            // Try resolve it from booleans
            if (tallSelfDisguisesVisibility == null) {
                // Previously, 'true' means they want tall disguises to obscure the player's view
                if (globalConfig.getBoolean("TallSelfDisguises")) {
                    tallSelfDisguisesVisibility = DisguiseConfig.TallSelfDisguise.VISIBLE;
                    // Previously, 'true' meant that if the above was false, they want the disguise to be scaled down
                    // Some versions didn't even have this setting, so assume its true
                } else if (globalConfig.getBoolean("TallSelfDisguisesScaling", true)) {
                    tallSelfDisguisesVisibility = DisguiseConfig.TallSelfDisguise.SCALED;
                    // And if tall disguises are disabled and scaling is disabled, they don't want tall disguises to show at all
                } else {
                    tallSelfDisguisesVisibility = DisguiseConfig.TallSelfDisguise.HIDDEN;
                }
            }

            globalConfig.set("TallSelfDisguises", tallSelfDisguisesVisibility);
        }
    }

    private DisguiseConfig.TallSelfDisguise getVisibility(YamlConfiguration configuration) {
        try {
            return DisguiseConfig.TallSelfDisguise.valueOf(configuration.getString("TallSelfDisguises").toUpperCase(Locale.ENGLISH));
        } catch (Exception ignored) {
        }
        return null;
    }
}
