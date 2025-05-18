package me.libraryaddict.disguise.utilities;

import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.reflection.FakeBoundingBox;

import java.util.HashMap;

@Setter
@Getter
public class DisguiseValues {
    private static final HashMap<DisguiseType, DisguiseValues> values = new HashMap<>();

    public static DisguiseValues getDisguiseValues(DisguiseType type) {
        return values.get(type);
    }

    private FakeBoundingBox adultBox;
    private FakeBoundingBox babyBox;
    private final double maxHealth;
    private final int ambientSoundInterval;

    public DisguiseValues(DisguiseType type, double maxHealth, int ambientSoundInterval) {
        values.put(type, this);
        this.maxHealth = maxHealth;
        this.ambientSoundInterval = ambientSoundInterval < 0 ? 120 : ambientSoundInterval;
    }
}
