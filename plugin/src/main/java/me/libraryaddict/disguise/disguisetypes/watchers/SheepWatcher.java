package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;

import java.util.Optional;

public class SheepWatcher extends AgeableWatcher {

    public SheepWatcher(Disguise disguise) {
        super(disguise);
    }

    public DyeColor getColor() {
        return AnimalColor.getColorByWool(((int) getData(MetaIndex.SHEEP_WOOL) & 15)).getDyeColor();
    }

    @Deprecated
    public void setColor(AnimalColor color) {
        setColor(color.getDyeColor());
    }

    public void setColor(DyeColor color) {
        byte b0 = getData(MetaIndex.SHEEP_WOOL);

        sendData(MetaIndex.SHEEP_WOOL, (byte) (b0 & 240 | color.getWoolData() & 15));
    }

    public boolean isRainbowWool() {
        if (!NmsVersion.v1_13.isSupported()) {
            if (!hasValue(MetaIndex.ENTITY_CUSTOM_NAME_OLD)) {
                return false;
            }

            return "jeb_".equals(getData(MetaIndex.ENTITY_CUSTOM_NAME_OLD));
        }

        if (!hasValue(MetaIndex.ENTITY_CUSTOM_NAME)) {
            return false;
        }

        Optional<Component> optional = getData(MetaIndex.ENTITY_CUSTOM_NAME);

        return optional.isPresent() && DisguiseUtilities.serialize(optional.get()).contains("\"jeb_\"");
    }

    public void setRainbowWool(boolean rainbow) {
        if (isRainbowWool() == rainbow) {
            return;
        }

        setInteralCustomName("jeb_");
    }

    public boolean isSheared() {
        return (getData(MetaIndex.SHEEP_WOOL) & 16) != 0;
    }

    public void setSheared(boolean flag) {
        byte b0 = getData(MetaIndex.SHEEP_WOOL);

        if (flag) {
            b0 = (byte) (b0 | 16);
        } else {
            b0 = (byte) (b0 & -17);
        }

        sendData(MetaIndex.SHEEP_WOOL, b0);
    }
}
