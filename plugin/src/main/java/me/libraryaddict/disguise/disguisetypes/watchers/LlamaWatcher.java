package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Llama;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class LlamaWatcher extends ChestedHorseWatcher {

    public LlamaWatcher(Disguise disguise) {
        super(disguise);
    }

    public Llama.Color getColor() {
        return getData(MetaIndex.LLAMA_COLOR);
    }

    public void setColor(Llama.Color color) {
        sendData(MetaIndex.LLAMA_COLOR, color);
    }

    public DyeColor getCarpet() {
        if (NmsVersion.v1_20_R4.isSupported()) {
            ItemStack item = getItemStack(EquipmentSlot.BODY);

            if (item == null) {
                return null;
            }

            AnimalColor color = AnimalColor.getColorByWool(item.getType());

            if (color == null) {
                return null;
            }

            return color.getDyeColor();
        }

        int color = getData(MetaIndex.LLAMA_CARPET);

        if (color == -1) {
            return null;
        }

        return AnimalColor.getColorByWool(color).getDyeColor();
    }

    public void setCarpet(DyeColor dyeColor) {
        if (!NmsVersion.v1_20_R4.isSupported()) {
            sendData(MetaIndex.LLAMA_CARPET, dyeColor != null ? (int) dyeColor.getWoolData() : null);
            return;
        }

        ItemStack item = null;

        if (dyeColor != null) {
            AnimalColor color = AnimalColor.getColor(dyeColor);

            if (color != null) {
                item = new ItemStack(color.getCarpetMaterial());
            }
        }

        setItemStack(EquipmentSlot.BODY, item);
    }

    @Deprecated
    public void setCarpet(AnimalColor color) {
        setCarpet(color != null ? color.getDyeColor() : null);
    }

    /**
     * Control if the carpet is shown or not, exposed as DyeColor = null just means passthrough
     *
     * @param carpetShown If the carpet is to be shown or not, this overrides passthrough
     */
    public void setCarpetShown(boolean carpetShown) {
        if (NmsVersion.v1_20_R4.isSupported()) {
            if ((getItemStack(EquipmentSlot.BODY) != null) == carpetShown) {
                return;
            }

            if (!carpetShown) {
                setItemStack(EquipmentSlot.BODY, new ItemStack(Material.AIR));
            } else {
                setItemStack(EquipmentSlot.BODY, new ItemStack(AnimalColor.RED.getCarpetMaterial()));
            }
        } else if (hasValue(MetaIndex.LLAMA_CARPET) && (getData(MetaIndex.LLAMA_CARPET) != -1) == carpetShown) {
            return;
        }

        sendData(MetaIndex.LLAMA_CARPET, -1);
    }

    public int getStrength() {
        return getData(MetaIndex.LLAMA_STRENGTH);
    }

    public void setStrength(int strength) {
        sendData(MetaIndex.LLAMA_STRENGTH, strength);
    }
}
