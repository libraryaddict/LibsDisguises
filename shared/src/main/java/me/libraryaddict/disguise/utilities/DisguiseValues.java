package me.libraryaddict.disguise.utilities;

import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.utilities.reflection.FakeBoundingBox;

@Setter
@Getter
public class DisguiseValues {
    private FakeBoundingBox adultBox;
    private FakeBoundingBox babyBox;
    private final double maxHealth;
    private final int ambientSoundInterval;

    public DisguiseValues(double maxHealth, int ambientSoundInterval) {
        this.maxHealth = maxHealth;
        this.ambientSoundInterval = ambientSoundInterval < 0 ? 120 : ambientSoundInterval;
    }
}
