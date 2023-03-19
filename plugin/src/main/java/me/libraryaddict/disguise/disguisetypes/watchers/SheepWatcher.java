package me.libraryaddict.disguise.disguisetypes.watchers;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
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

        setData(MetaIndex.SHEEP_WOOL, (byte) (b0 & 240 | color.getWoolData() & 15));
        sendData(MetaIndex.SHEEP_WOOL);
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

        Optional<WrappedChatComponent> optional = getData(MetaIndex.ENTITY_CUSTOM_NAME);

        return optional.filter(wrappedChatComponent -> "{\"text\":\"jeb_\"}".equals(wrappedChatComponent.getJson())).isPresent();

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
            setData(MetaIndex.SHEEP_WOOL, (byte) (b0 | 16));
        } else {
            setData(MetaIndex.SHEEP_WOOL, (byte) (b0 & -17));
        }

        sendData(MetaIndex.SHEEP_WOOL);
    }
}
