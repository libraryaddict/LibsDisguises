package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.jetbrains.annotations.ApiStatus;

public abstract class AgeableAquaWatcher extends AgeableWatcher {
    public AgeableAquaWatcher(Disguise disguise) {
        super(disguise);
    }

    @ApiStatus.AvailableSince("1.21.3")
    @NmsAddedIn(NmsVersion.v1_21_R2)
    public boolean isAdult() {
        return super.isAdult();
    }

    @ApiStatus.AvailableSince("1.21.3")
    @NmsAddedIn(NmsVersion.v1_21_R2)
    public boolean isBaby() {
        return super.isBaby();
    }

    @MethodDescription("Is this a baby?")
    @ApiStatus.AvailableSince("1.21.3")
    @NmsAddedIn(NmsVersion.v1_21_R2)
    public void setBaby(boolean isBaby) {
        super.setBaby(isBaby);
    }

    @ApiStatus.AvailableSince("1.21.3")
    @NmsAddedIn(NmsVersion.v1_21_R2)
    public void setAdult() {
        super.setAdult();
    }

    @ApiStatus.AvailableSince("1.21.3")
    @NmsAddedIn(NmsVersion.v1_21_R2)
    public void setBaby() {
        super.setBaby();
    }
}
