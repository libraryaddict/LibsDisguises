package me.libraryaddict.disguise.utilities.sounds;

import com.github.retrooper.packetevents.resources.ResourceLocation;
import lombok.Getter;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import org.apache.commons.lang.math.RandomUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;

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
    private final LinkedHashMap<ResourceLocation, SoundType> disguiseSoundTypes = new LinkedHashMap<>();
    @Getter
    private final LinkedHashMap<SoundType, ResourceLocation[]> disguiseSounds = new LinkedHashMap<>();
    private boolean customSounds;

    public SoundGroup(String name) {
        groups.put(name, this);

        try {
            DisguiseType.valueOf(name);
        } catch (Exception ex) {
            customSounds = true;
        }
    }

    public void addSound(ResourceLocation soundString, SoundType type) {
        if (soundString == null) {
            return;
        }

        disguiseSoundTypes.putIfAbsent(soundString, type);

        if (disguiseSounds.containsKey(type)) {
            ResourceLocation[] array = disguiseSounds.get(type);

            array = Arrays.copyOf(array, array.length + 1);
            array[array.length - 1] = soundString;

            disguiseSounds.put(type, array);
        } else {
            disguiseSounds.put(type, new ResourceLocation[]{soundString});
        }
    }

    public float getDamageAndIdleSoundVolume() {
        return damageSoundVolume;
    }

    public void setDamageAndIdleSoundVolume(float strength) {
        this.damageSoundVolume = strength;
    }

    public ResourceLocation getSound(SoundType type) {
        if (type == null) {
            return null;
        }

        if (customSounds) {
            return getRandomSound(type);
        }

        ResourceLocation[] sounds = disguiseSounds.get(type);

        if (sounds == null) {
            return null;
        }

        return sounds[0];
    }

    private ResourceLocation getRandomSound(SoundType type) {
        if (type == null) {
            return null;
        }

        ResourceLocation[] sounds = disguiseSounds.get(type);

        if (sounds == null) {
            return null;
        }

        return sounds[RandomUtils.nextInt(sounds.length)];
    }

    public SoundType getSound(ResourceLocation sound) {
        if (sound == null) {
            return null;
        }

        return disguiseSoundTypes.get(sound);
    }

    /**
     * Used to check if this sound name is owned by this disguise sound.
     */
    public SoundType getType(ResourceLocation sound) {
        if (sound == null) {
            return SoundType.CANCEL;
        }

        return getSound(sound);
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
