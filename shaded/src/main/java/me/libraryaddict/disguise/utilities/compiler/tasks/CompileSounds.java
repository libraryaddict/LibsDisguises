package me.libraryaddict.disguise.utilities.compiler.tasks;

import me.libraryaddict.disguise.utilities.sounds.DisguiseSoundEnums;
import me.libraryaddict.disguise.utilities.sounds.SoundGroup;
import org.bukkit.Sound;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompileSounds {
    public byte[] doSounds() {
        List<String> list = new ArrayList<>();

        for (DisguiseSoundEnums e : DisguiseSoundEnums.getValues()) {
            list.add(getSoundAsString(e, null).toString());

            if (e.getVariants() != null) {
                for (String variant : e.getVariants()) {
                    list.add(getSoundAsString(e, variant).toString());
                }
            }
        }

        return String.join("\n", list).getBytes(StandardCharsets.UTF_8);
    }

    private StringBuilder getSoundAsString(DisguiseSoundEnums e, String variant) {
        StringBuilder sound = new StringBuilder(e.getName());

        if (variant != null) {
            sound.append("$").append(variant);
        }

        for (SoundGroup.SoundType type : SoundGroup.SoundType.values()) {
            sound.append("/");

            int i = 0;

            for (Map.Entry<String, SoundGroup.SoundType> entry : e.getSounds().entrySet()) {
                if (entry.getValue() != type) {
                    continue;
                }

                String soundValue = entry.getKey();

                if (soundValue.contains("*")) {
                    soundValue = String.join(",", getMatchingFields(soundValue));
                } else if (soundValue.startsWith("!")) {
                    // Remove the !, so that we can register the non-variant properly
                    soundValue = soundValue.substring(1);

                    if (variant != null) {
                        // Get the second period, so entity.wolf<.>stuff.stuff
                        int period = soundValue.indexOf(".", soundValue.indexOf(".") + 2);

                        soundValue = soundValue.substring(0, period) + "_" + variant + soundValue.substring(period);
                    }
                }

                if (i++ > 0) {
                    sound.append(",");
                }

                sound.append(soundValue);
            }
        }
        return sound;
    }

    private List<String> getMatchingFields(String pattern) {
        List<String> matches = new ArrayList<>();

        for (Field field : Sound.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != Sound.class) {
                continue;
            }

            if (!field.getName().matches(pattern)) {
                continue;
            }

            matches.add(field.getName());
        }

        return matches;
    }
}
