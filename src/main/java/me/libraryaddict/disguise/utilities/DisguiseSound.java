package me.libraryaddict.disguise.utilities;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Sound;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Only living disguises go in here!
 */
public enum DisguiseSound {
    ARMOR_STAND,

    ARROW,

    BAT,

    BEE,

    BLAZE,

    BOAT,

    CAVE_SPIDER,

    CHICKEN,

    COD,

    COW,

    CREEPER,

    DOLPHIN,

    DONKEY,

    DROWNED,

    ELDER_GUARDIAN,

    ENDER_DRAGON,

    ENDERMAN,

    ENDERMITE,

    EVOKER,

    EVOKER_FANGS,

    GHAST,

    GIANT,

    GUARDIAN,

    HORSE,

    HUSK,

    ILLUSIONER,

    IRON_GOLEM,

    LLAMA,

    MAGMA_CUBE,

    MULE,

    MUSHROOM_COW,

    OCELOT,

    PARROT,

    PIG,

    PIG_ZOMBIE,

    PLAYER,

    PHANTOM,

    POLAR_BEAR,

    PUFFERFISH,

    RABBIT,

    SALMON,

    SHEEP,

    SHULKER,

    SILVERFISH,

    SKELETON,

    SKELETON_HORSE,

    SLIME,

    SNOWMAN,

    SPIDER,

    STRAY,

    SQUID,

    TROPICAL_FISH,

    TURTLE,

    VEX,

    VILLAGER,

    VINDICATOR,

    WITCH,

    WITHER,

    WITHER_SKELETON,

    WOLF,

    ZOMBIE,

    ZOMBIE_HORSE,

    ZOMBIE_VILLAGER;

    public enum SoundType {
        CANCEL,
        DEATH,
        HURT,
        IDLE,
        STEP
    }

    static {
        try (InputStream stream = LibsDisguises.getInstance().getResource("ANTI_PIRACY_ENCODED_WITH_SOUNDS")) {
            List<String> lines = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines()
                    .collect(Collectors.toList());

            for (String s : lines) {
                String[] spl = s.split(":", -1);

                DisguiseSound sound = DisguiseSound.valueOf(spl[0]);

                for (int i = 1; i <= SoundType.values().length; i++) {
                    if (spl[i].isEmpty()) {
                        continue;
                    }

                    String[] split = spl[i].split(",");

                    for (String sName : split) {
                        try {
                            sound.addSound(Sound.valueOf(sName), SoundType.values()[i - 1]);
                        }
                        catch (IllegalArgumentException ex) {
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DisguiseSound getType(String name) {
        try {
            return valueOf(name);
        }
        catch (Exception ex) {
            return null;
        }
    }

    private float damageSoundVolume = 1F;
    private LinkedHashMap<Object, SoundType> disguiseSounds = new LinkedHashMap<>();

    private Sound parseSound(String name) {
        try {
            return Sound.valueOf(name);
        }
        catch (Exception ex) {
        }

        return null;
    }

    private void addSound(Object sound, SoundType type) {
        if (sound == null) {
            return;
        }

        if (sound instanceof String[]) {
            for (String s : (String[]) sound) {
                Sound so = parseSound(s);

                if (so == null) {
                    continue;
                }

                addSound(so, type);
            }
        } else if (sound instanceof String) {
            Sound so = parseSound((String) sound);

            if (so == null) {
                return;
            }

            addSound(so, type);
        } else if (sound instanceof Sound[]) {
            for (Sound s : (Sound[]) sound) {
                if (s == null) {
                    continue;
                }

                addSound(s, type);
            }
        } else if (sound instanceof Sound) {
            addSound((Sound) sound, type);
        } else {
            throw new IllegalArgumentException("Was given an unknown object " + sound);
        }
    }

    private void addSound(Sound sound, SoundType type) {
        Object soundEffect = ReflectionManager.getCraftSound(sound);

        if (disguiseSounds.containsKey(soundEffect)) {
            DisguiseUtilities.getLogger().severe("Already doing " + sound);
        }

        disguiseSounds.put(soundEffect, type);
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

        for (Entry<Object, SoundType> entry : disguiseSounds.entrySet()) {
            if (entry.getValue() != type) {
                continue;
            }

            return entry.getKey();
        }

        return null;
    }

    public SoundType getSound(Object sound) {
        if (sound == null) {
            return null;
        }

        return disguiseSounds.get(sound);
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
}
