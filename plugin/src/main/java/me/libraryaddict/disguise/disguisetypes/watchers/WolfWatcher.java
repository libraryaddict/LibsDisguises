package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class WolfWatcher extends TameableWatcher {

    public WolfWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            if (NmsVersion.v1_20_R4.isSupported()) {
                setVariant(ReflectionManager.randomEnum(Wolf.Variant.class));
            }
        }
    }

    public DyeColor getCollarColor() {
        return getData(MetaIndex.WOLF_COLLAR).getDyeColor();
    }

    @Deprecated
    public void setCollarColor(AnimalColor color) {
        setCollarColor(color.getDyeColor());
    }

    public void setCollarColor(DyeColor newColor) {
        if (!isTamed()) {
            setTamed(true);
        }

        if (hasValue(MetaIndex.WOLF_COLLAR) && newColor == getCollarColor()) {
            return;
        }

        sendData(MetaIndex.WOLF_COLLAR, AnimalColor.getColorByWool(newColor.getWoolData()));
    }

    public boolean isBegging() {
        return getData(MetaIndex.WOLF_BEGGING);
    }

    public void setBegging(boolean begging) {
        sendData(MetaIndex.WOLF_BEGGING, begging);
    }

    public boolean isAngry() {
        if (!NmsVersion.v1_16.isSupported()) {
            return isTameableFlag(2);
        }

        return getAnger() > 0;
    }

    public void setAngry(boolean angry) {
        if (!NmsVersion.v1_16.isSupported()) {
            setTameableFlag(2, angry);
        } else {
            setAnger(angry ? 1 : 0);
        }
    }

    @NmsAddedIn(NmsVersion.v1_16)
    public int getAnger() {
        return getData(MetaIndex.WOLF_ANGER);
    }

    @NmsAddedIn(NmsVersion.v1_16)
    public void setAnger(int anger) {
        sendData(MetaIndex.WOLF_ANGER, anger);
    }

    /**
     * Used for tail rotation.
     *
     * @return
     */
    @NmsRemovedIn(NmsVersion.v1_15)
    @Deprecated
    public float getDamageTaken() {
        return getData(MetaIndex.WOLF_DAMAGE);
    }

    /**
     * Used for tail rotation.
     *
     * @param damage
     */
    @Deprecated
    @NmsRemovedIn(NmsVersion.v1_15)
    public void setDamageTaken(float damage) {
        sendData(MetaIndex.WOLF_DAMAGE, damage);
    }

    @NmsAddedIn(NmsVersion.v1_20_R4)
    public void setVariant(Wolf.Variant variant) {
        sendData(MetaIndex.WOLF_VARIANT, variant);
    }

    @NmsAddedIn(NmsVersion.v1_20_R4)
    public Wolf.Variant getVariant() {
        return getData(MetaIndex.WOLF_VARIANT);
    }

    @NmsAddedIn(NmsVersion.v1_20_R4)
    public ItemStack getWolfArmor() {
        return getEquipment().getItem(EquipmentSlot.BODY);
    }

    @NmsAddedIn(NmsVersion.v1_20_R4)
    public void setWolfArmor(ItemStack item) {
        getEquipment().setItem(EquipmentSlot.BODY, item);
    }

    @NmsAddedIn(NmsVersion.v1_20_R4)
    public Color getWolfArmorColor() {
        ItemStack item = getWolfArmor();

        if (item == null || item.getType() != Material.WOLF_ARMOR) {
            return null;
        }

        return ((LeatherArmorMeta) item.getItemMeta()).getColor();
    }

    @NmsAddedIn(NmsVersion.v1_20_R4)
    public void setWolfArmorColor(Color color) {
        if (color == null) {
            setWolfArmor(null);
            return;
        }

        ItemStack item = getWolfArmor();

        if (item == null || item.getType() != Material.WOLF_ARMOR) {
            item = new ItemStack(Material.WOLF_ARMOR);
        }

        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color);
        item.setItemMeta(meta);

        setWolfArmor(item);
    }
}
