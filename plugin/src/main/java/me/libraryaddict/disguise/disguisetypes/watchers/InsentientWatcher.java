package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.inventory.MainHand;

public class InsentientWatcher extends LivingWatcher {
    public InsentientWatcher(Disguise disguise) {
        super(disguise);
    }

    public MainHand getMainHand() {
        return getInsentientFlag(2) ? MainHand.RIGHT : MainHand.LEFT;
    }

    public void setMainHand(MainHand mainHand) {
        setInsentientFlag(2, mainHand == MainHand.RIGHT);
    }

    public boolean isAI() {
        return getInsentientFlag(1);
    }

    public void setAI(boolean ai) {
        setInsentientFlag(1, ai);
    }

    private void setInsentientFlag(int i, boolean flag) {
        byte b0 = getData(MetaIndex.INSENTIENT_META);

        if (flag) {
            setData(MetaIndex.INSENTIENT_META, (byte) (b0 | i));
        } else {
            setData(MetaIndex.INSENTIENT_META, (byte) (b0 & -(i + 1)));
        }

        sendData(MetaIndex.INSENTIENT_META);
    }

    private boolean getInsentientFlag(int i) {
        return (getData(MetaIndex.INSENTIENT_META) & i) != 0;
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public boolean isEnraged() {
        return getInsentientFlag(4);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setEnraged(boolean enraged) {
        if (hasValue(MetaIndex.INSENTIENT_META) && getInsentientFlag(4) == enraged) {
            return;
        }

        setInsentientFlag(4, enraged);
    }
}
