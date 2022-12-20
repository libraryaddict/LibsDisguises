package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;

/**
 * Created by libraryaddict on 15/06/2021.
 */
public class GoatWatcher extends AgeableWatcher {
    public GoatWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isScreaming() {
        return getData(MetaIndex.GOAT_SCREAMING);
    }

    public void setScreaming(boolean screaming) {
        setData(MetaIndex.GOAT_SCREAMING, screaming);
        sendData(MetaIndex.GOAT_SCREAMING);
    }

    public boolean hasLeftHorn() {
        return getData(MetaIndex.GOAT_HAS_LEFT_HORN);
    }

    @NmsAddedIn(NmsVersion.v1_19_R1)
    public void setHasLeftHorn(boolean hasHorn) {
        setData(MetaIndex.GOAT_HAS_LEFT_HORN, hasHorn);
        sendData(MetaIndex.GOAT_HAS_LEFT_HORN);
    }

    public boolean hasRightHorn() {
        return getData(MetaIndex.GOAT_HAS_RIGHT_HORN);
    }

    @NmsAddedIn(NmsVersion.v1_19_R1)
    public void setHasRightHorn(boolean hasHorn) {
        setData(MetaIndex.GOAT_HAS_RIGHT_HORN, hasHorn);
        sendData(MetaIndex.GOAT_HAS_RIGHT_HORN);
    }
}
