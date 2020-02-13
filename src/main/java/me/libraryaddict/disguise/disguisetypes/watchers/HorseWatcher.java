package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.Material;
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

    @RandomDefaultValue
    public void setColor(Color color) {
        setData(MetaIndex.HORSE_COLOR, color.ordinal() & 0xFF | getStyle().ordinal() << 8);
        sendData(MetaIndex.HORSE_COLOR);
    }

    public Style getStyle() {
        return Style.values()[(getData(MetaIndex.HORSE_COLOR) >>> 8)];
    }

    @RandomDefaultValue
    public void setStyle(Style style) {
        setData(MetaIndex.HORSE_COLOR, getColor().ordinal() & 0xFF | style.ordinal() << 8);
        sendData(MetaIndex.HORSE_COLOR);
    }

    public ItemStack getHorseArmor() {
        return getEquipment().getChestplate();
    }

    public void setHorseArmor(ItemStack item) {
        getEquipment().setChestplate(item);

        if (!NmsVersion.v1_14.isSupported()) {
            int value = 0;

            if (item != null) {
                Material mat = item.getType();

                if (mat == Material.IRON_HORSE_ARMOR) {
                    value = 1;
                } else if (mat == Material.GOLDEN_HORSE_ARMOR) {
                    value = 2;
                } else if (mat == Material.DIAMOND_HORSE_ARMOR) {
                    value = 3;
                }
            }

            setData(MetaIndex.HORSE_ARMOR, value);
            sendData(MetaIndex.HORSE_ARMOR);
        }
    }
}
