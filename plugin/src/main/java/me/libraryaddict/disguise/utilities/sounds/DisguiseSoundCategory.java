package me.libraryaddict.disguise.utilities.sounds;

import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;

public enum DisguiseSoundCategory {
    AMBIENT(SoundCategory.AMBIENT),
    BLOCKS(SoundCategory.BLOCK),
    HOSTILE(SoundCategory.HOSTILE),
    MASTER(SoundCategory.MASTER),
    MUSIC(SoundCategory.MUSIC),
    NEUTRAL(SoundCategory.NEUTRAL),
    PLAYERS(SoundCategory.PLAYER),
    RECORDS(SoundCategory.RECORD),
    UI(SoundCategory.UI),
    VOICE(SoundCategory.VOICE),
    WEATHER(SoundCategory.WEATHER),
    ACTUAL(null), // Will return the sound category of the disguised entity
    DISGUISE(null); // Will return the sound category of the disguise itself

    private final SoundCategory soundCategory;
    private final org.bukkit.SoundCategory bSoundCategory;

    DisguiseSoundCategory(SoundCategory soundCategory) {
        this.soundCategory = soundCategory;

        org.bukkit.SoundCategory category = org.bukkit.SoundCategory.MASTER;

        if (!name().equals("ACTUAL") && !name().equals("DISGUISE")) {
            try {
                category = org.bukkit.SoundCategory.valueOf(name());
            } catch (Exception ignored) {
            }
        }

        bSoundCategory = category;
    }

    public org.bukkit.SoundCategory getBukkitSoundCategory(Disguise disguise) {
        if (this == ACTUAL && disguise.getEntity() != null) {
            return ReflectionManager.getBukkitSoundCategory(DisguiseType.getType(disguise.getEntity()));
        } else if (this == DISGUISE) {
            return ReflectionManager.getBukkitSoundCategory(disguise.getType());
        }

        return bSoundCategory;
    }

    public SoundCategory getSoundCategory(Disguise disguise) {
        if (this == ACTUAL && disguise.getEntity() != null) {
            return ReflectionManager.getSoundCategory(DisguiseType.getType(disguise.getEntity()));
        } else if (this == DISGUISE) {
            return ReflectionManager.getSoundCategory(disguise.getType());
        }

        return soundCategory;
    }

    public boolean isAvailable() {
        return this != UI || NmsVersion.v1_21_R6.isSupported();
    }

    public static DisguiseSoundCategory byName(String name) {
        DisguiseSoundCategory category = valueOf(name);

        if (category != null && !category.isAvailable()) {
            throw new IllegalArgumentException("Cannot use " + category.name() + " on this version of MC!");
        }

        return category;
    }
}
