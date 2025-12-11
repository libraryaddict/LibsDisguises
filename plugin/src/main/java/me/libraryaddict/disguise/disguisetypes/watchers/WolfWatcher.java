package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.protocol.entity.wolfvariant.WolfSoundVariant;
import com.github.retrooper.packetevents.protocol.entity.wolfvariant.WolfSoundVariants;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
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

                if (NmsVersion.v1_21_R4.isSupported()) {
                    setSoundVariant(ReflectionManager.randomRegistry(WolfSoundVariants.getRegistry()));
                }
            }
        }
    }

    @NmsAddedIn(NmsVersion.v1_21_R4)
    public WolfSoundVariant getSoundVariant() {
        return getData(MetaIndex.WOLF_SOUND_VARIANT);
    }

    @RandomDefaultValue
    @NmsAddedIn(NmsVersion.v1_21_R4)
    @MethodDescription("The sound a wolf makes")
    public void setSoundVariant(WolfSoundVariant soundVariant) {
        // Spigot does not have Wolf.SoundVariant
        // Although Lib's Disguises could support Wolf.SoundVariant as a seperate method, we do not implement it as this could indirectly
        // break third party plugins
        sendData(MetaIndex.WOLF_SOUND_VARIANT, soundVariant);
    }

    public DyeColor getCollarColor() {
        return getData(MetaIndex.WOLF_COLLAR).getDyeColor();
    }

    @Deprecated
    public void setCollarColor(AnimalColor color) {
        setCollarColor(color != null ? color.getDyeColor() : null);
    }

    @MethodDescription("The color of the wolf's collar")
    public void setCollarColor(DyeColor newColor) {
        if (newColor == null) {
            sendData(MetaIndex.WOLF_COLLAR, null);
            return;
        }

        if (!isTamed()) {
            setTamed(true);
        }

        if (hasValue(MetaIndex.WOLF_COLLAR) && newColor == getCollarColor()) {
            return;
        }

        sendData(MetaIndex.WOLF_COLLAR, AnimalColor.getColor(newColor));
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

    @MethodDescription("If the wolf should look angry")
    public void setAngry(boolean angry) {
        if (!NmsVersion.v1_16.isSupported()) {
            setTameableFlag(2, angry);
        } else {
            setAnger(angry ? 1 : 0);
        }
    }

    @NmsAddedIn(NmsVersion.v1_16)
    @NmsRemovedIn(NmsVersion.v1_21_R7)
    public int getAnger() {
        return getData(MetaIndex.WOLF_ANGER_OLD);
    }

    @NmsAddedIn(NmsVersion.v1_16)
    @NmsRemovedIn(NmsVersion.v1_21_R7)
    public void setAnger(int anger) {
        // TODO Does this have a visible effect?
        sendData(MetaIndex.WOLF_ANGER_OLD, anger);
    }

    @NmsAddedIn(NmsVersion.v1_21_R7)
    public long getAngryUntil() {
        return getData(MetaIndex.WOLF_ANGER);
    }

    @NmsAddedIn(NmsVersion.v1_21_R7)
    public void setAngryUntil(long wolfAngryUntilTime) {
        sendData(MetaIndex.WOLF_ANGER, wolfAngryUntilTime);
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
    @MethodDescription("Controls the angle of the wolf's tail")
    public void setDamageTaken(float damage) {
        sendData(MetaIndex.WOLF_DAMAGE, damage);
    }

    @RandomDefaultValue
    @NmsAddedIn(NmsVersion.v1_20_R4)
    @MethodDescription("The variant that controls the wolf's appearance")
    public void setVariant(Wolf.Variant variant) {
        sendData(MetaIndex.WOLF_VARIANT, variant);
    }

    @NmsAddedIn(NmsVersion.v1_20_R4)
    public Wolf.Variant getVariant() {
        return getData(MetaIndex.WOLF_VARIANT);
    }

    @NmsAddedIn(NmsVersion.v1_20_R4)
    public ItemStack getWolfArmor() {
        return getItemStack(EquipmentSlot.BODY);
    }

    @NmsAddedIn(NmsVersion.v1_20_R4)
    @MethodDescription("The item that renders as the wolf's armor, only a certain subset of items will display")
    public void setWolfArmor(ItemStack item) {
        setItemStack(EquipmentSlot.BODY, item);
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
    @MethodDescription("The color the wolf's armor will be dyed to, if null, armor will also be treated as null")
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
