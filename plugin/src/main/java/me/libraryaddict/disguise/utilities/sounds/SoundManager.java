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
import java.util.Map;
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

        for (String groupName : config.getKeys(false)) {
            if (!config.isConfigurationSection(groupName) || groupName.equals("GroupName")) {
                continue;
            }

            if (SoundGroup.getGroups().keySet().stream().anyMatch(k -> k.equalsIgnoreCase(groupName))) {
                LibsDisguises.getInstance().getLogger().warning("The SoundGroup " + groupName + " has already been registered!");
                continue;
            }

            ConfigurationSection groupSection = config.getConfigurationSection(groupName);

            DisguiseSoundCategory category = getCategory(groupSection, "Category", null);

            SoundGroup group = new SoundGroup(groupName, category);

            for (String soundToReplace : groupSection.getKeys(false)) {
                if (soundToReplace.equals("Category")) {
                    continue;
                }

                List<?> list = groupSection.getList(soundToReplace);

                SoundGroup.SoundType type = null;
                ResourceLocation replacedSound = null;

                if (soundToReplace.matches("^[A-Z][a-z]+$")) {
                    try {
                        type = SoundGroup.SoundType.valueOf(soundToReplace.toUpperCase(Locale.ENGLISH));
                    } catch (Exception ignored) {
                    }
                }

                if (type == null) {
                    if (!soundToReplace.contains(":")) {
                        LibsDisguises.getInstance().getLogger().warning("Invalid section key '" + soundToReplace + "' on " + groupName +
                            "! Must be a minecraft:sound.name or sound type!");
                        continue;
                    }

                    replacedSound = new ResourceLocation(soundToReplace);
                }

                // They asked to have no sounds for this
                if (list == null || list.isEmpty()) {
                    if (type != null) {
                        group.addSound(type, (DisguiseSound) null);
                    } else {
                        group.addRemappedSound(replacedSound, null);
                    }

                    continue;
                }

                for (Object soundEntry : list) {
                    DisguiseSound[] sounds = parseSound(groupName, soundToReplace, soundEntry);

                    if (sounds == null) {
                        continue;
                    }

                    for (DisguiseSound sound : sounds) {
                        if (type != null) {
                            group.addSound(type, sound);
                        } else {
                            group.addRemappedSound(replacedSound, sound);
                        }
                    }
                }
            }

            LibsDisguises.getInstance().getLogger().info("Loaded sound group '" + groupName + "'");
        }
    }

    public static DisguiseSound[] parseSound(String groupName, String replacementKey, Object soundEntry) {
        String sound;
        Float volume = null;
        Float pitch = null;
        // pitchMin is used for both the range and for setting normal pitch
        Float pitchMin = null;
        Float pitchMax = null;
        float weight = 1f;

        if (soundEntry instanceof String) {
            sound = (String) soundEntry;
        } else if (soundEntry instanceof Map) {
            Map.Entry<?, ?> entryAsMap = ((Map<?, ?>) soundEntry).entrySet().iterator().next();
            sound = String.valueOf(entryAsMap.getKey());

            if (entryAsMap.getValue() instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) entryAsMap.getValue();

                for (Map.Entry<?, ?> e : map.entrySet()) {
                    float val;

                    if (!(e.getKey() instanceof String)) {
                        continue;
                    } else if (e.getValue() instanceof Float) {
                        val = (Float) e.getValue();
                    } else if (e.getValue() instanceof Number) {
                        val = ((Number) e.getValue()).floatValue();
                    } else if (e.getValue() instanceof String && ((String) e.getValue()).matches("\\s*\\d+(\\.\\d+)?\\s*")) {
                        val = Float.parseFloat(String.valueOf(e.getValue()).trim());
                    } else if (e.getValue() instanceof Character) {
                        val = (Character) e.getValue();
                    } else {
                        LibsDisguises.getInstance().getLogger().warning(
                            "Invalid number '" + e.getValue() + "' set on '" + e.getKey() + "' for sound '" + sound + "' on " + groupName +
                                "." + replacementKey + "!");
                        continue;
                    }

                    if (!Float.isFinite(val)) {
                        LibsDisguises.getInstance().getLogger().warning(
                            "Invalid number '" + e.getValue() + "' set on '" + e.getKey() + "' for sound '" + sound + "' on " + groupName +
                                "." + replacementKey + "!");
                        continue;
                    }

                    if (e.getKey().equals("Volume")) {
                        volume = val;
                    } else if (e.getKey().equals("Pitch")) {
                        pitch = val;
                    } else if (e.getKey().equals("PitchMin")) {
                        pitchMin = val;
                    } else if (e.getKey().equals("PitchMax")) {
                        pitchMax = val;
                    } else if (e.getKey().equals("Weight")) {
                        weight = val;
                    }
                }

                if (pitch != null && (pitchMin != null || pitchMax != null)) {
                    LibsDisguises.getInstance().getLogger().warning(
                        "Invalid sound '" + sound + "': cannot mix Pitch with PitchMin and PitchMax on " + groupName + "." +
                            replacementKey + "! Defaulting to Pitch.");
                    return null;
                }

                if ((pitchMin == null) != (pitchMax == null)) {
                    LibsDisguises.getInstance().getLogger().warning(
                        "Invalid sound '" + sound + "': PitchMin and PitchMax must both be defined on " + groupName + "." + replacementKey +
                            "!");
                    return null;
                }

                if (pitchMin != null && pitchMax != null && pitchMin > pitchMax) {
                    LibsDisguises.getInstance().getLogger().warning(
                        "Invalid sound '" + sound + "': PitchMin cannot be larger than PitchMax on " + groupName + "." + replacementKey +
                            "!");
                    return null;
                }

                if (pitch != null) {
                    pitchMin = pitch;
                }
            }
        } else {
            return null;
        }

        if (!sound.matches(".+:.+")) {
            SoundGroup subGroup = SoundGroup.getGroup(sound);

            if (subGroup == null) {
                LibsDisguises.getInstance().getLogger()
                    .warning("Invalid sound '" + sound + "'! Must be a minecraft:sound.name or SoundGroup name!");
                return null;
            }

            DisguiseSound[] sounds = subGroup.getDisguiseSounds().get(replacementKey);

            if (sounds == null) {
                LibsDisguises.getInstance().getLogger().warning(
                    "Sound group '" + sound + "' does not contain a category for " + replacementKey + "! Can't use as default in " +
                        groupName);
                return null;
            }

            return sounds;
        }

        return new DisguiseSound[]{new DisguiseSound(new ResourceLocation(sound), weight, volume, pitchMin, pitchMax)};
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

                                group.addSound(type, s1.getSoundId());
                            }
                        } else {
                            group.addSound(type, new ResourceLocation(soundStr));
                        }
                    }
                }
            }
        } catch (IOException | NoClassDefFoundError e) {
            e.printStackTrace();
        }
    }
}
