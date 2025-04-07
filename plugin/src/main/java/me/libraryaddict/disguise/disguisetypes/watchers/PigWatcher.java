package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.Material;
import org.bukkit.entity.Pig;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PigWatcher extends AgeableWatcher {

    public PigWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isSaddled() {
        if (!NmsVersion.v1_21_R4.isSupported()) {
            return getData(MetaIndex.PIG_SADDLED);
        }

        ItemStack item = getItemStack(EquipmentSlot.SADDLE);

        return item != null && item.getType() != Material.AIR;
    }

    public void setSaddled(boolean isSaddled) {
        if (!NmsVersion.v1_21_R4.isSupported()) {
            sendData(MetaIndex.PIG_SADDLED, isSaddled);
            return;
        }

        setItemStack(EquipmentSlot.SADDLE, new ItemStack(isSaddled ? Material.SADDLE : Material.AIR));
    }

    @NmsAddedIn(NmsVersion.v1_21_R4)
    public void setVariant(Pig.Variant variant) {
        sendData(MetaIndex.PIG_VARIANT, variant);
    }

    @NmsAddedIn(NmsVersion.v1_21_R4)
    public Pig.Variant getVariant() {
        return getData(MetaIndex.PIG_VARIANT);
    }
}
