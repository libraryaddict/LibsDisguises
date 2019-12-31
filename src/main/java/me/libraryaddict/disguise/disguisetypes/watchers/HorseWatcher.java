package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.inventory.ItemStack;

public class HorseWatcher extends AbstractHorseWatcher {
    public HorseWatcher(Disguise disguise) {
        super(disguise);

        setStyle(Style.values()[DisguiseUtilities.random.nextInt(Style.values().length)]);
        setColor(Color.values()[DisguiseUtilities.random.nextInt(Color.values().length)]);
    }

    public Color getColor() {
        return Color.values()[(getData(MetaIndex.HORSE_COLOR) & 0xFF)];
    }

    public Style getStyle() {
        return Style.values()[(getData(MetaIndex.HORSE_COLOR) >>> 8)];
    }

    @RandomDefaultValue
    public void setColor(Color color) {
        setData(MetaIndex.HORSE_COLOR, color.ordinal() & 0xFF | getStyle().ordinal() << 8);
        sendData(MetaIndex.HORSE_COLOR);
    }

    @RandomDefaultValue
    public void setStyle(Style style) {
        setData(MetaIndex.HORSE_COLOR, getColor().ordinal() & 0xFF | style.ordinal() << 8);
        sendData(MetaIndex.HORSE_COLOR);
    }

    public void setHorseArmor(ItemStack item) {
        getEquipment().setChestplate(item);
    }

    public ItemStack getHorseArmor() {
        return getEquipment().getChestplate();
    }
}
