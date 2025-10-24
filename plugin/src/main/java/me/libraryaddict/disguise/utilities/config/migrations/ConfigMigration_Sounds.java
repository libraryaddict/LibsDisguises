package me.libraryaddict.disguise.utilities.config.migrations;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.config.migrations.generics.AbstractBundledConfigInjectorMigration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigMigration_Sounds extends AbstractBundledConfigInjectorMigration {

    @Override
    public File getTargetFile() {
        return new File(LibsDisguises.getInstance().getDataFolder(), "configs/sounds.yml");
    }

    @Override
    public String getBundledConfigName() {
        return "configs/sounds.yml";
    }

    @Override
    public String getInjectionMarker() {
        // Inject after the first line that contains this comment.
        // If not found, will inject at the top of the file
        return "# This means no 'PLAYER' 'COW' etc";
    }

    @Override
    public String getInjectionStartText() {
        return "\n# Shall I disguise the sounds?";
    }

    @Override
    public String getInjectionEndText() {
        return "SoundCategory: DISGUISE\n\n";
    }

    @Override
    public String getContentToInject(YamlConfiguration loadedConfig) {
        boolean disguiseSounds = loadedConfig.getBoolean("DisguiseSounds", true);
        boolean idleSounds = loadedConfig.getBoolean("PlayIdleSounds", false);

        String toInject = super.getContentToInject(loadedConfig);
        toInject = toInject.replace("DisguiseSounds: true", "DisguiseSounds: " + disguiseSounds);
        toInject = toInject.replace("PlayIdleSounds: false", "PlayIdleSounds: " + idleSounds);

        return toInject;
    }

    @Override
    public String[] getFilesToMigrateFrom() {
        return new String[]{"protocol.yml"};
    }

    @Override
    public int getVersion() {
        return 5;
    }
}