package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;

@NmsAddedIn(NmsVersion.v1_15)
public class BeeWatcher extends AgeableWatcher {
    public BeeWatcher(Disguise disguise) {
        super(disguise);
    }

    @NmsRemovedIn(NmsVersion.v1_21_R7)
    public int getBeeAnger() {
        return getData(MetaIndex.BEE_ANGER_OLD);
    }

    @NmsRemovedIn(NmsVersion.v1_21_R7)
    public void setBeeAnger(int beeAnger) {
        sendData(MetaIndex.BEE_ANGER_OLD, beeAnger);
    }

    @NmsAddedIn(NmsVersion.v1_21_R7)
    public long getAngryUntil() {
        return getData(MetaIndex.BEE_ANGER);
    }

    @NmsAddedIn(NmsVersion.v1_21_R7)
    public void setAngryUntil(long beeAngryUntilTime) {
        sendData(MetaIndex.BEE_ANGER, beeAngryUntilTime);
    }

    public void setHasNectar(boolean hasNectar) {
        setBeeFlag(8, hasNectar);
    }

    public boolean hasNectar() {
        return getBeeFlag(8);
    }

    public void setHasStung(boolean hasStung) {
        setBeeFlag(4, hasStung);
    }

    public boolean hasStung() {
        return getBeeFlag(4);
    }

    public boolean isFlipped() {
        return getBeeFlag(2);
    }

    public void setFlipped(boolean isFlipped) {
        setBeeFlag(2, isFlipped);
    }

    private boolean getBeeFlag(int value) {
        return (getData(MetaIndex.BEE_META) & value) != 0;
    }

    private void setBeeFlag(int no, boolean flag) {
        byte b1 = getData(MetaIndex.BEE_META);

        if (flag) {
            b1 = (byte) (b1 | no);
        } else {
            b1 = (byte) (b1 & ~no);
        }

        sendData(MetaIndex.BEE_META, b1);
    }
}
