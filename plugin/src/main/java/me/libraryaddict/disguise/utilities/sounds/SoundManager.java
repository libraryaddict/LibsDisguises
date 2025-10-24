package me.libraryaddict.disguise.utilities.sounds;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class SoundManager {
    public void load() {
        SoundGroup.getGroups().clear();

        loadSounds();
        loadCustomSounds();
    }

    /**
     * Always returns default if not set or invalid
     */
    private DisguiseSoundCategory getCategory(ConfigurationSection config, String key, String def) {
        String val = config.getString(key, def);

        if (val == null) {
            return null;
        }

        try {
            return DisguiseSoundCategory.byName(val);
        } catch (Exception e) {
            LibsDisguises.getInstance().getLogger().warning("Invalid sound category '" + val + "'");
        }

        if (def == null) {
            return null;
        }

        return DisguiseSoundCategory.byName(def);
    }

    private void loadCustomSounds() {
        File f = new File(LibsDisguises.getInstance().getDataFolder(), "configs/sounds.yml");

        if (!f.exists()) {
            f.getParentFile().mkdirs();

            File old = new File(LibsDisguises.getInstance().getDataFolder(), "sounds.yml");

            if (old.exists()) {
                old.renameTo(f);
            } else {
                LibsDisguises.getInstance().saveResource("configs/sounds.yml", false);
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(f);

        DisguiseConfig.setSoundCategory(getCategory(config, "SoundCategory", "DISGUISE"));
        DisguiseConfig.setPlayIdleSounds(config.getBoolean("PlayIdleSounds", false));
        DisguiseConfig.setSoundsEnabled(config.getBoolean("DisguiseSounds", true));

        for (String key : config.getKeys(false)) {
            if (!config.isConfigurationSection(key) || key.equals("GroupName")) {
                continue;
            }

            if (SoundGroup.getGroups().keySet().stream().anyMatch(k -> k.equalsIgnoreCase(key))) {
                LibsDisguises.getInstance().getLogger().warning("The SoundGroup " + key + " has already been registered!");
                continue;
            }

            ConfigurationSection section = config.getConfigurationSection(key);

            DisguiseSoundCategory category = getCategory(section, "Category", null);

            SoundGroup group = new SoundGroup(key, category);

            for (SoundGroup.SoundType type : SoundGroup.SoundType.values()) {
                if (type == SoundGroup.SoundType.CANCEL) {
                    continue;
                }

                List<String> list = section.getStringList(type.name().charAt(0) + type.name().substring(1).toLowerCase(Locale.ENGLISH));

                if (list == null || list.isEmpty()) {
                    continue;
                }

                for (String sound : list) {
                    if (!sound.matches(".+:.+")) {
                        SoundGroup subGroup = SoundGroup.getGroup(sound);

                        if (subGroup == null) {
                            LibsDisguises.getInstance().getLogger()
                                .warning("Invalid sound '" + sound + "'! Must be a minecraft:sound.name or SoundGroup name!");
                            continue;
                        }

                        ResourceLocation[] sounds = subGroup.getDisguiseSounds().get(type);

                        if (sounds == null) {
                            LibsDisguises.getInstance().getLogger().warning(
                                "Sound group '" + sound + "' does not contain a category for " + type + "! Can't use as default in " + key);
                            continue;
                        }

                        for (ResourceLocation obj : sounds) {
                            group.addSound(obj, type);
                        }

                        continue;
                    }

                    group.addSound(new ResourceLocation(sound), type);
                }
            }

            LibsDisguises.getInstance().getLogger().info("Loaded sound group '" + key + "'");
        }
    }

    private void loadSounds() {
        ClientVersion serverVersion = PacketEvents.getAPI().getServerManager().getVersion().toClientVersion();

        try (InputStream stream = LibsDisguises.getInstance().getResource("SOUND_MAPPINGS.txt")) {
            String[] lines = new String(ReflectionManager.readFuzzyFully(stream), StandardCharsets.UTF_8).split("\n");

            for (String line : lines) {
                String[] groups = line.split("/", -1);

                SoundGroup group = new SoundGroup(groups[0]);

                int i = 0;
                for (SoundGroup.SoundType type : SoundGroup.SoundType.values()) {
                    String s = groups[++i];

                    if (s.isEmpty()) {
                        continue;
                    }

                    String[] sounds = s.split(",", -1);

                    for (String soundStr : sounds) {
                        // If sound is using regex, then try resolve via PE
                        if (soundStr.startsWith("^")) {
                            Pattern pattern = Pattern.compile(soundStr);

                            for (com.github.retrooper.packetevents.protocol.sound.Sound s1 : Sounds.values()) {
                                // If not registered for this server version, or regex does not match
                                if (s1.getId(serverVersion) < 0 || !s1.getSoundId().getKey().matches(soundStr)) {
                                    continue;
                                }

                                group.addSound(s1.getSoundId(), type);
                            }
                        } else {
                            group.addSound(new ResourceLocation(soundStr), type);
                        }
                    }
                }
            }
        } catch (IOException | NoClassDefFoundError e) {
            e.printStackTrace();
        }
    }
}
