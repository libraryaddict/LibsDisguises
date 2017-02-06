package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.Material;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

public class HorseWatcher extends AbstractHorseWatcher {
    public HorseWatcher(Disguise disguise) {
        super(disguise);

        setStyle(Style.values()[DisguiseUtilities.random.nextInt(Style.values().length)]);
        setColor(Color.values()[DisguiseUtilities.random.nextInt(Color.values().length)]);
    }

    public Color getColor() {
        return Color.values()[((Integer) getData(MetaIndex.HORSE_COLOR) & 0xFF)];
    }

    public ItemStack getHorseArmor() {
        int horseValue = getHorseArmorAsInt();

        switch (horseValue) {
        case 1:
            return new ItemStack(Material.IRON_BARDING);
        case 2:
            return new ItemStack(Material.GOLD_BARDING);
        case 3:
            return new ItemStack(Material.DIAMOND_BARDING);
        default:
            break;
        }

        return null;
    }

    public Style getStyle() {
        return Style.values()[(getData(MetaIndex.HORSE_COLOR) >>> 8)];
    }

    public void setColor(Color color) {
        setData(MetaIndex.HORSE_COLOR, color.ordinal() & 0xFF | getStyle().ordinal() << 8);
        sendData(MetaIndex.HORSE_COLOR);
    }

    protected int getHorseArmorAsInt() {
        return getData(MetaIndex.HORSE_ARMOR);
    }

    protected void setHorseArmor(int armor) {
        setData(MetaIndex.HORSE_ARMOR, armor);
        sendData(MetaIndex.HORSE_ARMOR);
    }

    public void setStyle(Style style) {
        setData(MetaIndex.HORSE_COLOR, getColor().ordinal() & 0xFF | style.ordinal() << 8);
        sendData(MetaIndex.HORSE_COLOR);
    }

    public void setHorseArmor(ItemStack item) {
        int value = 0;

        if (item != null) {
            Material mat = item.getType();

            if (mat == Material.IRON_BARDING) {
                value = 1;
            }
            else if (mat == Material.GOLD_BARDING) {
                value = 2;
            }
            else if (mat == Material.DIAMOND_BARDING) {
                value = 3;
            }
        }

        setHorseArmor(value);
    }

}
