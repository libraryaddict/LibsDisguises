package me.libraryaddict.disguise.disguisetypes.watchers;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Optional;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

public class HorseWatcher extends AgeableWatcher
{

    public HorseWatcher(Disguise disguise)
    {
        super(disguise);

        setStyle(Style.values()[DisguiseUtilities.random.nextInt(Style.values().length)]);
        setColor(Color.values()[DisguiseUtilities.random.nextInt(Color.values().length)]);
    }

    public Variant getVariant()
    {
        return Variant.values()[getValue(FlagType.HORSE_VARIANT)];
    }

    public void setVariant(Variant variant)
    {
        setVariant(variant.ordinal());
    }

    public void setVariant(int variant)
    {
        if (variant < 0 || variant > 4)
        {
            variant = 0; // Crashing people is mean
        }

        setValue(FlagType.HORSE_VARIANT, variant);
        sendData(FlagType.HORSE_VARIANT);
    }

    public Color getColor()
    {
        return Color.values()[((Integer) getValue(FlagType.HORSE_COLOR) & 0xFF)];
    }

    public ItemStack getHorseArmor()
    {
        int horseValue = getHorseArmorAsInt();

        switch (horseValue)
        {
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

    protected int getHorseArmorAsInt()
    {
        return getValue(FlagType.HORSE_ARMOR);
    }

    public Optional<UUID> getOwner()
    {
        return getValue(FlagType.HORSE_OWNER);
    }

    public Style getStyle()
    {
        return Style.values()[(getValue(FlagType.HORSE_STYLE) >>> 8)];
    }

    public boolean hasChest()
    {
        return isHorseFlag(8);
    }

    public boolean isBreedable()
    {
        return isHorseFlag(16);
    }

    public boolean isGrazing()
    {
        return isHorseFlag(32);
    }

    public boolean isMouthOpen()
    {
        return isHorseFlag(128);
    }

    public boolean isRearing()
    {
        return isHorseFlag(64);
    }

    public boolean isSaddled()
    {
        return isHorseFlag(4);
    }

    public boolean isTamed()
    {
        return isHorseFlag(2);
    }

    private boolean isHorseFlag(int i)
    {
        return (getHorseFlag() & i) != 0;
    }

    private byte getHorseFlag()
    {
        return getValue(FlagType.HORSE_META);
    }

    public void setCanBreed(boolean breed)
    {
        setHorseFlag(16, breed);
    }

    public void setCarryingChest(boolean chest)
    {
        setHorseFlag(8, chest);
    }

    public void setColor(Color color)
    {
        setValue(FlagType.HORSE_COLOR, color.ordinal() & 0xFF | getStyle().ordinal() << 8);
        sendData(FlagType.HORSE_COLOR);
    }

    private void setHorseFlag(int i, boolean flag)
    {
        byte j = getValue(FlagType.HORSE_META);

        if (flag)
        {
            setValue(FlagType.HORSE_META, (byte) (j | i));
        }
        else
        {
            setValue(FlagType.HORSE_META, (byte) (j & ~i));
        }

        sendData(FlagType.HORSE_META);
    }

    public void setGrazing(boolean grazing)
    {
        setHorseFlag(32, grazing);
    }

    protected void setHorseArmor(int armor)
    {
        setValue(FlagType.HORSE_ARMOR, armor);
        sendData(FlagType.HORSE_ARMOR);
    }

    public void setHorseArmor(ItemStack item)
    {
        int value = 0;

        if (item != null)
        {
            Material mat = item.getType();

            if (mat == Material.IRON_BARDING)
            {
                value = 1;
            }
            else if (mat == Material.GOLD_BARDING)
            {
                value = 2;
            }
            else if (mat == Material.DIAMOND_BARDING)
            {
                value = 3;
            }
        }

        setHorseArmor(value);
    }

    public void setMouthOpen(boolean mouthOpen)
    {
        setHorseFlag(128, mouthOpen);
    }

    public void setOwner(UUID uuid)
    {
        setValue(FlagType.HORSE_OWNER, Optional.of(uuid));
        sendData(FlagType.HORSE_OWNER);
    }

    public void setRearing(boolean rear)
    {
        setHorseFlag(64, rear);
    }

    public void setSaddled(boolean saddled)
    {
        setHorseFlag(4, saddled);
    }

    public void setStyle(Style style)
    {
        setValue(FlagType.HORSE_STYLE, getColor().ordinal() & 0xFF | style.ordinal() << 8);
        sendData(FlagType.HORSE_STYLE);
    }

    public void setTamed(boolean tamed)
    {
        setHorseFlag(2, tamed);
    }

}
