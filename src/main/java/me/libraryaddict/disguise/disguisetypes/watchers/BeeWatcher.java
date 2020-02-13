package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;

/**
 * Created by libraryaddict on 14/12/2019.
 */
@NmsAddedIn(val = NmsVersion.v1_15)
public class BeeWatcher extends AgeableWatcher {
    public BeeWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setBeeAnger(int beeAnger) {
        setData(MetaIndex.BEE_ANGER, beeAnger);
        sendData(MetaIndex.BEE_ANGER);
    }

    public int getBeeAnger() {
        return getData(MetaIndex.BEE_ANGER);
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

    public void setFlipped(boolean isFlipped) {
        setBeeFlag(2, isFlipped);
    }

    public boolean isFlipped() {
        return getBeeFlag(2);
    }

    private boolean getBeeFlag(int value) {
        return (getData(MetaIndex.PANDA_META) & value) != 0;
    }

    private void setBeeFlag(int no, boolean flag) {
        byte b1 = getData(MetaIndex.BEE_META);

        if (flag) {
            b1 = (byte) (b1 | no);
        } else {
            b1 = (byte) (b1 & ~no);
        }

        setData(MetaIndex.BEE_META, b1);
        sendData(MetaIndex.BEE_META);
    }
}
