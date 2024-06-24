package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
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
        return getData(MetaIndex.CAT_TYPE);
    }

    @RandomDefaultValue
    @MethodDescription("What variant of Cat is this?")
    public void setType(Cat.Type type) {
        setData(MetaIndex.CAT_TYPE, type);
        sendData(MetaIndex.CAT_TYPE);
    }

    public DyeColor getCollarColor() {
        return getData(MetaIndex.CAT_COLLAR).getDyeColor();
    }

    @Deprecated
    public void setCollarColor(AnimalColor color) {
        setCollarColor(color.getDyeColor());
    }

    @MethodDescription("What's the Cat's collar color?")
    public void setCollarColor(DyeColor newColor) {
        if (!isTamed()) {
            setTamed(true);
        }

        if (hasValue(MetaIndex.CAT_COLLAR) && newColor == getCollarColor()) {
            return;
        }

        setData(MetaIndex.CAT_COLLAR, AnimalColor.getColorByWool(newColor.getWoolData()));
        sendData(MetaIndex.CAT_COLLAR);
    }

    public boolean isLyingDown() {
        return getData(MetaIndex.CAT_LYING_DOWN);
    }

    @MethodDescription("Is the Cat lying down?")
    public void setLyingDown(boolean value) {
        setData(MetaIndex.CAT_LYING_DOWN, value);
        sendData(MetaIndex.CAT_LYING_DOWN);
    }

    public boolean isLookingUp() {
        return getData(MetaIndex.CAT_LOOKING_UP);
    }

    @MethodDescription("Is the Cat looking upwards?")
    public void setLookingUp(boolean value) {
        setData(MetaIndex.CAT_LOOKING_UP, value);
        sendData(MetaIndex.CAT_LOOKING_UP);
    }
}
