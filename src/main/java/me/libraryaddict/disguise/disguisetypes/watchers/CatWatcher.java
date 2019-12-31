package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import org.bukkit.DyeColor;
import org.bukkit.entity.Cat;

import java.util.Random;

/**
 * Created by libraryaddict on 6/05/2019.
 */
public class CatWatcher extends TameableWatcher {
    public CatWatcher(Disguise disguise) {
        super(disguise);

        setType(Cat.Type.values()[new Random().nextInt(Cat.Type.values().length)]);
    }

    public Cat.Type getType() {
        return Cat.Type.values()[getData(MetaIndex.CAT_TYPE)];
    }

    @RandomDefaultValue
    public void setType(Cat.Type type) {
        setData(MetaIndex.CAT_TYPE, type.ordinal());
        sendData(MetaIndex.CAT_TYPE);
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

    public DyeColor getCollarColor() {
        return AnimalColor.getColorByWool(getData(MetaIndex.CAT_COLLAR)).getDyeColor();
    }

    public void setLyingDown(boolean value) {
        setData(MetaIndex.CAT_LYING_DOWN, value);
        sendData(MetaIndex.CAT_LYING_DOWN);
    }

    public boolean isLyingDown() {
        return getData(MetaIndex.CAT_LYING_DOWN);
    }

    public void setLookingUp(boolean value) {
        setData(MetaIndex.CAT_LOOKING_UP, value);
        sendData(MetaIndex.CAT_LOOKING_UP);
    }

    public boolean isLookingUp() {
        return getData(MetaIndex.CAT_LOOKING_UP);
    }
}
