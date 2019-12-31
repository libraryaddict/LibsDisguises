package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.DyeColor;

public class SheepWatcher extends AgeableWatcher {

    public SheepWatcher(Disguise disguise) {
        super(disguise);
    }

    public DyeColor getColor() {
        return AnimalColor.getColorByWool(((int) getData(MetaIndex.SHEEP_WOOL) & 15)).getDyeColor();
    }

    public boolean isSheared() {
        return (getData(MetaIndex.SHEEP_WOOL) & 16) != 0;
    }

    @Deprecated
    public void setColor(AnimalColor color) {
        setColor(color.getDyeColor());
    }

    public void setColor(DyeColor color) {
        byte b0 = getData(MetaIndex.SHEEP_WOOL);

        setData(MetaIndex.SHEEP_WOOL, (byte) (b0 & 240 | color.getWoolData() & 15));
        sendData(MetaIndex.SHEEP_WOOL);
    }

    public void setSheared(boolean flag) {
        byte b0 = getData(MetaIndex.SHEEP_WOOL);

        if (flag) {
            setData(MetaIndex.SHEEP_WOOL, (byte) (b0 | 16));
        } else {
            setData(MetaIndex.SHEEP_WOOL, (byte) (b0 & -17));
        }

        sendData(MetaIndex.SHEEP_WOOL);
    }
}
