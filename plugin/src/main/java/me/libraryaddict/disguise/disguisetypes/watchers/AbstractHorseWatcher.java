package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;

import java.util.Optional;
import java.util.UUID;

public abstract class AbstractHorseWatcher extends AgeableWatcher {
    private static final int TAMED = 2, SADDLED = 4, REPRODUCED = 8, GRAZING = 16, REARING = 32, EATING = 64;

    public AbstractHorseWatcher(Disguise disguise) {
        super(disguise);
    }

    public UUID getOwner() {
        return getData(MetaIndex.HORSE_OWNER).orElse(null);
    }

    @NmsRemovedIn(NmsVersion.v1_19_R3)
    public void setOwner(UUID uuid) {
        setData(MetaIndex.HORSE_OWNER, Optional.of(uuid));
        sendData(MetaIndex.HORSE_OWNER);
    }

    /**
     * If the horse can be breeded, no visible effect
     *
     * @return Is horse breedable
     */
    public boolean isReproduced() {
        return isHorseFlag(REPRODUCED);
    }

    public void setReproduced(boolean reproduced) {
        setHorseFlag(REPRODUCED, reproduced);
    }

    /**
     * If the horse is grazing
     *
     * @return Is horse grazing
     */
    public boolean isGrazing() {
        return isHorseFlag(GRAZING);
    }

    public void setGrazing(boolean grazing) {
        setHorseFlag(GRAZING, grazing);
    }

    /**
     * If the horse has it's mouth open
     *
     * @return Horse has mouth open
     */
    public boolean isEating() {
        return isHorseFlag(EATING);
    }

    public void setEating(boolean mouthOpen) {
        setHorseFlag(EATING, mouthOpen);
    }

    public boolean isRearing() {
        return isHorseFlag(REARING);
    }

    public void setRearing(boolean rear) {
        setHorseFlag(REARING, rear);
    }

    public boolean isSaddled() {
        return isHorseFlag(SADDLED);
    }

    public void setSaddled(boolean saddled) {
        setHorseFlag(SADDLED, saddled);
    }

    public boolean isTamed() {
        return isHorseFlag(TAMED);
    }

    public void setTamed(boolean tamed) {
        setHorseFlag(TAMED, tamed);
    }

    private boolean isHorseFlag(int i) {
        return (getHorseFlag() & i) != 0;
    }

    private byte getHorseFlag() {
        return getData(MetaIndex.HORSE_META);
    }

    private void setHorseFlag(int i, boolean flag) {
        byte j = getData(MetaIndex.HORSE_META);

        if (flag) {
            setData(MetaIndex.HORSE_META, (byte) (j | i));
        } else {
            setData(MetaIndex.HORSE_META, (byte) (j & ~i));
        }

        sendData(MetaIndex.HORSE_META);
    }
}
