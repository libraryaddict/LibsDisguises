package me.libraryaddict.disguise.disguisetypes.watchers;

import java.util.UUID;

import com.google.common.base.Optional;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public abstract class AbstractHorseWatcher extends AgeableWatcher {
    public AbstractHorseWatcher(Disguise disguise) {
        super(disguise);
    }

    public Optional<UUID> getOwner() {
        return getData(MetaIndex.HORSE_OWNER);
    }

    public boolean hasChest() {
        return isHorseFlag(8);
    }

    public boolean isBreedable() {
        return isHorseFlag(16);
    }

    public boolean isGrazing() {
        return isHorseFlag(32);
    }

    public boolean isMouthOpen() {
        return isHorseFlag(128);
    }

    public boolean isRearing() {
        return isHorseFlag(64);
    }

    public boolean isSaddled() {
        return isHorseFlag(4);
    }

    public boolean isTamed() {
        return isHorseFlag(2);
    }

    private boolean isHorseFlag(int i) {
        return (getHorseFlag() & i) != 0;
    }

    private byte getHorseFlag() {
        return getData(MetaIndex.HORSE_META);
    }

    public void setCanBreed(boolean breed) {
        setHorseFlag(16, breed);
    }

    public void setCarryingChest(boolean chest) {
        setHorseFlag(8, chest);
    }

    private void setHorseFlag(int i, boolean flag) {
        byte j = getData(MetaIndex.HORSE_META);

        if (flag) {
            setData(MetaIndex.HORSE_META, (byte) (j | i));
        }
        else {
            setData(MetaIndex.HORSE_META, (byte) (j & ~i));
        }

        sendData(MetaIndex.HORSE_META);
    }

    public void setGrazing(boolean grazing) {
        setHorseFlag(32, grazing);
    }

    public void setMouthOpen(boolean mouthOpen) {
        setHorseFlag(128, mouthOpen);
    }

    public void setOwner(UUID uuid) {
        setData(MetaIndex.HORSE_OWNER, Optional.of(uuid));
        sendData(MetaIndex.HORSE_OWNER);
    }

    public void setRearing(boolean rear) {
        setHorseFlag(64, rear);
    }

    public void setSaddled(boolean saddled) {
        setHorseFlag(4, saddled);
    }

    public void setTamed(boolean tamed) {
        setHorseFlag(2, tamed);
    }

}
