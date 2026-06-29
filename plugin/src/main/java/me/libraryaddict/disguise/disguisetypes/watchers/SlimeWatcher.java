package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.jetbrains.annotations.ApiStatus;

public class SlimeWatcher extends AgeableWatcher {

    public SlimeWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            setSize(DisguiseUtilities.random.nextInt(4) + 1);
        } else {
            setSize(2);
        }
    }

    public int getSize() {
        return getData(MetaIndex.SLIME_SIZE);
    }

    @RandomDefaultValue
    public void setSize(int size) {
        if (size < 1) {
            size = 1;
        } else if (size > 50) {
            size = 50;
        }

        setUnsafeSize(size);
    }

    public int getUnsafeSize() {
        return getSize();
    }

    @RandomDefaultValue
    public void setUnsafeSize(int size) {
        if (hasValue(MetaIndex.SLIME_SIZE) && getData(MetaIndex.SLIME_SIZE) == size) {
            return;
        }

        sendData(MetaIndex.SLIME_SIZE, size);

        updateNameHeight();
    }

    @ApiStatus.AvailableSince("26.2")
    @NmsAddedIn(NmsVersion.v26_R2)
    @Override
    public boolean isAdult() {
        return super.isAdult();
    }

    @ApiStatus.AvailableSince("26.2")
    @NmsAddedIn(NmsVersion.v26_R2)
    @Override
    public boolean isBaby() {
        return super.isBaby();
    }

    @MethodDescription("Is this a baby?")
    @ApiStatus.AvailableSince("26.2")
    @NmsAddedIn(NmsVersion.v26_R2)
    @Override
    public void setBaby(boolean isBaby) {
        super.setBaby(isBaby);
    }

    @ApiStatus.AvailableSince("26.2")
    @NmsAddedIn(NmsVersion.v26_R2)
    @Override
    public void setAdult() {
        super.setAdult();
    }

    @ApiStatus.AvailableSince("26.2")
    @NmsAddedIn(NmsVersion.v26_R2)
    @Override
    public void setBaby() {
        super.setBaby();
    }

    @ApiStatus.AvailableSince("26.2")
    @NmsAddedIn(NmsVersion.v26_R2)
    @Override
    public void setAgeLocked(boolean ageLocked) {
        sendData(MetaIndex.AGEABLE_AGE_LOCKED, ageLocked);
    }

    @ApiStatus.AvailableSince("26.2")
    @NmsAddedIn(NmsVersion.v26_R2)
    @Override
    public boolean isAgeLocked() {
        return getData(MetaIndex.AGEABLE_AGE_LOCKED);
    }
}
