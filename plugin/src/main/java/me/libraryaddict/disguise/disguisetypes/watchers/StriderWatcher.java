package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class StriderWatcher extends AgeableWatcher {
    public StriderWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isSaddled() {
        if (!NmsVersion.v1_21_R4.isSupported()) {
            return getData(MetaIndex.STRIDER_SADDLED);
        }

        ItemStack item = getItemStack(EquipmentSlot.SADDLE);

        return item != null && item.getType() != Material.AIR;
    }

    public void setSaddled(boolean isSaddled) {
        if (!NmsVersion.v1_21_R4.isSupported()) {
            sendData(MetaIndex.STRIDER_SADDLED, isSaddled);
            return;
        }

        setItemStack(EquipmentSlot.SADDLE, new ItemStack(isSaddled ? Material.SADDLE : Material.AIR));
    }
}
