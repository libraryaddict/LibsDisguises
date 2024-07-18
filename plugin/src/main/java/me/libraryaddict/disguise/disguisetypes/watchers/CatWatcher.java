package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.DyeColor;
import org.bukkit.entity.Cat;

@NmsAddedIn(NmsVersion.v1_14)
public class CatWatcher extends TameableWatcher {
    public CatWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            setType(ReflectionManager.randomEnum(Cat.Type.class));
        }
    }

    public Cat.Type getType() {
        return getData(MetaIndex.CAT_TYPE);
    }

    @RandomDefaultValue
    @MethodDescription("What variant of Cat is this?")
    public void setType(Cat.Type type) {
        sendData(MetaIndex.CAT_TYPE, type);
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

        sendData(MetaIndex.CAT_COLLAR, AnimalColor.getColorByWool(newColor.getWoolData()));
    }

    public boolean isLyingDown() {
        return getData(MetaIndex.CAT_LYING_DOWN);
    }

    @MethodDescription("Is the Cat lying down?")
    public void setLyingDown(boolean value) {
        sendData(MetaIndex.CAT_LYING_DOWN, value);
    }

    public boolean isLookingUp() {
        return getData(MetaIndex.CAT_LOOKING_UP);
    }

    @MethodDescription("Is the Cat looking upwards?")
    public void setLookingUp(boolean value) {
        sendData(MetaIndex.CAT_LOOKING_UP, value);
    }
}
