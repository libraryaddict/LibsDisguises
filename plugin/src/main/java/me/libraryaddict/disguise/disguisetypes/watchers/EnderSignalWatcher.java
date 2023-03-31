package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by libraryaddict on 6/05/2019.
 */
public class EnderSignalWatcher extends FlagWatcher {
    public EnderSignalWatcher(Disguise disguise) {
        super(disguise);

        if (NmsVersion.v1_14.isSupported()) {
            setItemStack(new ItemStack(Material.ENDER_EYE));
        }
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public ItemStack getItemStack() {
        return getData(MetaIndex.ENDER_SIGNAL_ITEM);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setItemStack(ItemStack item) {
        setData(MetaIndex.ENDER_SIGNAL_ITEM, item);
        sendData(MetaIndex.ENDER_SIGNAL_ITEM);
    }
}
