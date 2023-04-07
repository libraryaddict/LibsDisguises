package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.entity.Fox;

import java.util.Random;

/**
 * Created by libraryaddict on 6/05/2019.
 */
@NmsAddedIn(NmsVersion.v1_14)
public class FoxWatcher extends AgeableWatcher {
    public FoxWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            setType(Fox.Type.values()[new Random().nextInt(Fox.Type.values().length)]);
        }
    }

    public boolean isSitting() {
        return getFoxFlag(1);
    }

    public void setSitting(boolean value) {
        setFoxFlag(1, value);
    }

    public boolean isCrouching() {
        return getFoxFlag(4);
    }

    public void setCrouching(boolean value) {
        setFoxFlag(4, value);
    }

    public boolean isSleeping() {
        return getFoxFlag(32);
    }

    public void setSleeping(boolean value) {
        setFoxFlag(32, value);
    }

    public Fox.Type getType() {
        return Fox.Type.values()[getData(MetaIndex.FOX_TYPE)];
    }

    @RandomDefaultValue
    public void setType(Fox.Type type) {
        setData(MetaIndex.FOX_TYPE, type.ordinal());
        sendData(MetaIndex.FOX_TYPE);
    }

    public boolean isHeadTilted() {
        return getFoxFlag(8);
    }

    public void setHeadTilted(boolean value) {
        setFoxFlag(8, value);
    }

    public boolean isSpringing() {
        return getFoxFlag(16);
    }

    public void setSpringing(boolean value) {
        setFoxFlag(16, value);
    }

    public boolean isTipToeing() {
        return getFoxFlag(64);
    }

    public void setTipToeing(boolean value) {
        setFoxFlag(64, value);
    }

    public boolean isAngry() {
        return getFoxFlag(128);
    }

    public void setAngry(boolean value) {
        setFoxFlag(128, value);
    }

    private boolean getFoxFlag(int value) {
        return (getData(MetaIndex.FOX_META) & value) != 0;
    }

    private void setFoxFlag(int no, boolean flag) {
        byte b1 = getData(MetaIndex.FOX_META);

        if (flag) {
            b1 = (byte) (b1 | no);
        } else {
            b1 = (byte) (b1 & ~no);
        }

        setData(MetaIndex.FOX_META, b1);
        sendData(MetaIndex.FOX_META);
    }
}
