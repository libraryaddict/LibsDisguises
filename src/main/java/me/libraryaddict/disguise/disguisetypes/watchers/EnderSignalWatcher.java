package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by libraryaddict on 6/05/2019.
 */
public class EnderSignalWatcher extends FlagWatcher {
    public EnderSignalWatcher(Disguise disguise) {
        super(disguise);

        setItemStack(new ItemStack(Material.ENDER_EYE));
    }

    public void setItemStack(ItemStack item) {
        setData(MetaIndex.ENDER_SIGNAL_ITEM, item);
        sendData(MetaIndex.ENDER_SIGNAL_ITEM);
    }

    public ItemStack getItemStack() {
        return getData(MetaIndex.ENDER_SIGNAL_ITEM);
    }
}
