package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.DyeColor;
import org.bukkit.entity.Cat;

/**
 * Created by libraryaddict on 6/05/2019.
 */
@NmsAddedIn(NmsVersion.v1_14)
public class CatWatcher extends TameableWatcher {
    public CatWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            Cat.Type[] values = Cat.Type.values();

            setType(values[DisguiseUtilities.getRandom().nextInt(values.length)]);
        }
    }

    public Cat.Type getType() {
        if (NmsVersion.v1_19_R1.isSupported()) {
            return getData(MetaIndex.CAT_TYPE_NEW);
        }

        return Cat.Type.values()[getData(MetaIndex.CAT_TYPE)];
    }

    @RandomDefaultValue
    public void setType(Cat.Type type) {
        if (NmsVersion.v1_19_R1.isSupported()) {
            setData(MetaIndex.CAT_TYPE_NEW, type);
            sendData(MetaIndex.CAT_TYPE_NEW);
            return;
        }

        setData(MetaIndex.CAT_TYPE, type.ordinal());
        sendData(MetaIndex.CAT_TYPE);
    }

    public DyeColor getCollarColor() {
        return AnimalColor.getColorByWool(getData(MetaIndex.CAT_COLLAR)).getDyeColor();
    }

    @Deprecated
    public void setCollarColor(AnimalColor color) {
        setCollarColor(color.getDyeColor());
    }

    public void setCollarColor(DyeColor newColor) {
        if (!isTamed()) {
            setTamed(true);
        }

        if (newColor == getCollarColor()) {
            return;
        }

        setData(MetaIndex.CAT_COLLAR, (int) newColor.getWoolData());
        sendData(MetaIndex.CAT_COLLAR);
    }

    public boolean isLyingDown() {
        return getData(MetaIndex.CAT_LYING_DOWN);
    }

    public void setLyingDown(boolean value) {
        setData(MetaIndex.CAT_LYING_DOWN, value);
        sendData(MetaIndex.CAT_LYING_DOWN);
    }

    public boolean isLookingUp() {
        return getData(MetaIndex.CAT_LOOKING_UP);
    }

    public void setLookingUp(boolean value) {
        setData(MetaIndex.CAT_LOOKING_UP, value);
        sendData(MetaIndex.CAT_LOOKING_UP);
    }
}
