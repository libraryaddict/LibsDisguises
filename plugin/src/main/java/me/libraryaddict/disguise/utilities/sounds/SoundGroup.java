package me.libraryaddict.disguise.utilities.sounds;

import com.github.retrooper.packetevents.protocol.entity.data.struct.WeatheringCopperState;
import com.github.retrooper.packetevents.protocol.entity.wolfvariant.WolfSoundVariant;
import com.github.retrooper.packetevents.protocol.entity.wolfvariant.WolfSoundVariants;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.CopperGolemWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.WolfWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HappyGhast;
import org.bukkit.entity.Wolf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    @Getter
    @Setter
    private float damageAndIdleSoundVolume = 1F;
    @Getter
    private final LinkedHashMap<ResourceLocation, SoundType> disguiseSoundTypes = new LinkedHashMap<>();
    @Getter
    private final LinkedHashMap<SoundType, ResourceLocation[]> disguiseSounds = new LinkedHashMap<>();
    private boolean customSounds;
    private final DisguiseSoundCategory soundCategory;

    public SoundGroup(String name) {
        this(name, null);
    }

    public SoundGroup(String name, DisguiseSoundCategory category) {
        groups.put(name, this);
        this.soundCategory = category;

        try {
            DisguiseType.valueOf(name);
        } catch (Exception ex) {
            customSounds = true;
        }
    }

    public DisguiseSoundCategory getCategory() {
        if (soundCategory == null) {
            return DisguiseConfig.getSoundCategory();
        }

        return soundCategory;
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

        String variantName = null;

        if (NmsVersion.v1_21_R4.isSupported() && disguise.getWatcher() instanceof WolfWatcher) {
            WolfSoundVariant variant = ((WolfWatcher) disguise.getWatcher()).getSoundVariant();

            variantName = variant.getName().getKey();
        } else if (NmsVersion.v1_21_R6.isSupported() && disguise.getWatcher() instanceof CopperGolemWatcher) {
            WeatheringCopperState variant = ((CopperGolemWatcher) disguise.getWatcher()).getOxidation();

            // Only these two variants
            if (variant == WeatheringCopperState.WEATHERED || variant == WeatheringCopperState.OXIDIZED) {
                variantName = variant.name().toLowerCase(Locale.ENGLISH);
            }
        }

        String entityName = disguise.getType().name();

        if (disguise.getType() == DisguiseType.HAPPY_GHAST && disguise.getWatcher() instanceof AgeableWatcher &&
            ((AgeableWatcher) disguise.getWatcher()).isBaby()) {
            entityName = "GHASTLING";
        }

        return getGroup(entityName, variantName);
    }

    public static SoundGroup getGroup(Entity entity) {
        String name = entity.getType().name();
        String variantName = null;

        if (entity instanceof Wolf && NmsVersion.v1_21_R4.isSupported()) {
            // At the point of writing, spigot does not have a Wolf.SoundVariants
            // Paper on the contrary, has implemented it in their version
            if (DisguiseUtilities.isRunningPaper()) {
                variantName = ((Wolf) entity).getSoundVariant().getKey().getKey();
            } else {
                variantName = ReflectionManager.getNmsReflection().getVariant(entity, WolfSoundVariants.getRegistry());
            }
        } else if (NmsVersion.v1_21_R5.isSupported() && entity instanceof HappyGhast && !((Ageable) entity).isAdult()) {
            name = "GHASTLING";
        }

        return getGroup(name, variantName);
    }

    /**
     * Returns the group by this name, and its variants
     */
    public static SoundGroup[] getGroups(String name) {
        List<SoundGroup> list = new ArrayList<>();

        for (Map.Entry<String, SoundGroup> entry : groups.entrySet()) {
            if (!entry.getKey().split("\\$")[0].equals(name)) {
                continue;
            }

            list.add(entry.getValue());
        }

        return list.toArray(new SoundGroup[0]);
    }

    public static SoundGroup getGroup(String name) {
        return getGroup(name, null);
    }

    public static SoundGroup getGroup(String name, String variant) {
        if (variant != null) {
            return groups.getOrDefault(name + "$" + variant, groups.get(name));
        }

        return groups.get(name);
    }
}
