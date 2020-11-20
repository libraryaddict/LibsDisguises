package me.libraryaddict.disguise.utilities.sounds;

import lombok.Getter;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Sound;

import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * Created by libraryaddict on 23/05/2020.
 */
public class SoundGroup {
    public enum SoundType {
        CANCEL,
        DEATH,
        HURT,
        IDLE,
        STEP
    }

    @Getter
    private final static LinkedHashMap<String, SoundGroup> groups = new LinkedHashMap<>();
    private float damageSoundVolume = 1F;
    @Getter
    private final LinkedHashMap<Object, SoundType> disguiseSoundTypes = new LinkedHashMap<>();
    @Getter
    private final LinkedHashMap<SoundType, Object[]> disguiseSounds = new LinkedHashMap<>();
    private boolean customSounds;

    public SoundGroup(String name) {
        groups.put(name, this);

        try {
            DisguiseType.valueOf(name);
        } catch (Exception ex) {
            customSounds = true;
        }
    }

    public void addSound(Object sound, SoundType type) {
        Object origSound = sound;

        if (sound instanceof Sound) {
            sound = ReflectionManager.getCraftSound((Sound) sound);
        } else if (sound instanceof String) {
            sound = ReflectionManager.createMinecraftKey((String) sound);
        } else if (!sound.getClass().getSimpleName().equals("SoundEffect")) {
            throw new IllegalArgumentException("Unexpected " + sound.getClass());
        }

        if (sound == null) {
            return;
        }

        disguiseSoundTypes.putIfAbsent(sound, type);

        if (disguiseSounds.containsKey(type)) {
            Object[] array = disguiseSounds.get(type);

            array = Arrays.copyOf(array, array.length + 1);
            array[array.length - 1] = sound;

            disguiseSounds.put(type, array);
        } else {
            disguiseSounds.put(type, new Object[]{sound});
        }
    }

    public float getDamageAndIdleSoundVolume() {
        return damageSoundVolume;
    }

    public void setDamageAndIdleSoundVolume(float strength) {
        this.damageSoundVolume = strength;
    }

    public Object getSound(SoundType type) {
        if (type == null) {
            return null;
        }

        if (customSounds) {
            return getRandomSound(type);
        }

        Object[] sounds = disguiseSounds.get(type);

        if (sounds == null) {
            return null;
        }

        return sounds[0];
    }

    private Object getRandomSound(SoundType type) {
        if (type == null) {
            return null;
        }

        Object[] sounds = disguiseSounds.get(type);

        if (sounds == null) {
            return null;
        }

        return sounds[RandomUtils.nextInt(sounds.length)];
    }

    public SoundType getSound(Object sound) {
        if (sound == null) {
            return null;
        }

        return disguiseSoundTypes.get(sound);
    }

    /**
     * Used to check if this sound name is owned by this disguise sound.
     */
    public SoundType getType(Object sound, boolean ignoreDamage) {
        if (sound == null) {
            return SoundType.CANCEL;
        }

        SoundType soundType = getSound(sound);

        if (soundType == SoundType.DEATH || (ignoreDamage && soundType == SoundType.HURT)) {
            return null;
        }

        return soundType;
    }

    public boolean isCancelSound(String sound) {
        return getSound(sound) == SoundType.CANCEL;
    }

    public static SoundGroup getGroup(Disguise disguise) {
        if (disguise.getSoundGroup() != null) {
            SoundGroup dSoundGroup = getGroup(disguise.getSoundGroup());

            if (dSoundGroup != null) {
                return dSoundGroup;
            }
        }

        return getGroup(disguise.getType().name());
    }

    public static SoundGroup getGroup(String name) {
        return groups.get(name);
    }
}
