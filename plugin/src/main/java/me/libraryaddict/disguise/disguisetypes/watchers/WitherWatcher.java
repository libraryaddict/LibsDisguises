package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

import java.security.InvalidParameterException;

public class WitherWatcher extends InsentientWatcher {

    public WitherWatcher(Disguise disguise) {
        super(disguise);
    }

    /**
     * Returns the amount of time this Wither is invulnerable for
     *
     * @return
     */
    public int getInvulnerability() {
        return getData(MetaIndex.WITHER_INVUL);
    }

    /**
     * Sets the amount of time this Wither is invulnerable for
     */
    public void setInvulnerability(int invulnerability) {
        sendData(MetaIndex.WITHER_INVUL, invulnerability);
    }

    public int[] getTargets() {
        return new int[]{getData(MetaIndex.WITHER_TARGET_1), getData(MetaIndex.WITHER_TARGET_2), getData(MetaIndex.WITHER_TARGET_3)};
    }

    public void setTargets(int... targets) {
        if (targets.length != 3) {
            throw new InvalidParameterException("<red>Expected 3 numbers for wither setTargets. Received " + targets.length + "</red>");
        }
        setData(MetaIndex.WITHER_TARGET_1, targets[0]);
        setData(MetaIndex.WITHER_TARGET_2, targets[1]);
        setData(MetaIndex.WITHER_TARGET_3, targets[2]);
        sendData(MetaIndex.WITHER_TARGET_1, MetaIndex.WITHER_TARGET_2, MetaIndex.WITHER_TARGET_3);
    }
}
