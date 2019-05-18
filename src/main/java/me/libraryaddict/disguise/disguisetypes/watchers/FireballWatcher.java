package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by libraryaddict on 6/05/2019.
 */
public class FireballWatcher extends FlagWatcher {
    public FireballWatcher(Disguise disguise) {
        super(disguise);

        setData(MetaIndex.FIREBALL_ITEM, new ItemStack(Material.FIRE_CHARGE));
    }

    public ItemStack getItemStack() {
        return getData(MetaIndex.FIREBALL_ITEM);
    }

    public void setItemStack(ItemStack item) {
        setData(MetaIndex.FIREBALL_ITEM, item);
        sendData(MetaIndex.FIREBALL_ITEM);
    }
}
