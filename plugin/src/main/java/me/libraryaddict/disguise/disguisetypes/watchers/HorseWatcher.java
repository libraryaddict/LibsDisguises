package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Material;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class HorseWatcher extends AbstractHorseWatcher {
    public HorseWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            setStyle(ReflectionManager.randomEnum(Style.class));
            setColor(ReflectionManager.randomEnum(Color.class));
        }
    }

    public Color getColor() {
        return ReflectionManager.fromEnum(Color.class, getData(MetaIndex.HORSE_COLOR_STYLE) & 0xFF);
    }

    @RandomDefaultValue
    public void setColor(Color color) {
        sendData(MetaIndex.HORSE_COLOR_STYLE, color.ordinal() & 0xFF | getStyle().ordinal() << 8);
    }

    public Style getStyle() {
        return ReflectionManager.fromEnum(Style.class, (getData(MetaIndex.HORSE_COLOR_STYLE) >>> 8));
    }

    @RandomDefaultValue
    public void setStyle(Style style) {
        sendData(MetaIndex.HORSE_COLOR_STYLE, getColor().ordinal() & 0xFF | style.ordinal() << 8);
    }

    public ItemStack getHorseArmor() {
        if (NmsVersion.v1_20_R4.isSupported()) {
            return getEquipment().getItem(EquipmentSlot.BODY);
        }

        return getEquipment().getChestplate();
    }

    public void setHorseArmor(ItemStack item) {
        if (NmsVersion.v1_20_R4.isSupported()) {
            getEquipment().setItem(EquipmentSlot.BODY, item);
        } else {
            getEquipment().setChestplate(item);
        }

        if (NmsVersion.v1_14.isSupported()) {
            return;
        }

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

        sendData(MetaIndex.HORSE_ARMOR, value);
    }
}
