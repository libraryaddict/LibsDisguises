package me.libraryaddict.disguise.utilities.sounds;

import com.github.retrooper.packetevents.resources.ResourceLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

@AllArgsConstructor
public class DisguiseSound {
    @Getter
    private final ResourceLocation sound;
    @Getter
    private final float weight;
    @Getter
    private final Float volume;
    private final Float pitchMin, pitchMax;

    public DisguiseSound(ResourceLocation sound) {
        this(sound, 1f, null, null, null);
    }

    public String getKey() {
        return getSound().getKey();
    }

    public boolean hasPitch() {
        return pitchMin != null;
    }

    public boolean hasVolume() {
        return volume != null;
    }

    /**
     * Returns a random value between pitchMin and pitchMax.
     * If pitchMax is not set, returns pitchMin.
     *
     * @return sound pitch
     */
    public float getPitch() {
        if (pitchMax == null) {
            return pitchMin;
        }

        return pitchMin + ((pitchMax - pitchMin) * DisguiseUtilities.getRandom().nextFloat());
    }

    @Override
    public String toString() {
        return getSound().toString();
    }
}
