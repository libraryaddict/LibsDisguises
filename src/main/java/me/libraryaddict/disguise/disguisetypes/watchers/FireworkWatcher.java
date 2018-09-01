package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class FireworkWatcher extends FlagWatcher {
    public FireworkWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getFirework() {
        if (getData(MetaIndex.FIREWORK_ITEM) == null) {
            return new ItemStack(Material.AIR);
        }

        return (ItemStack) getData(MetaIndex.FIREWORK_ITEM);
    }

    public void setFirework(ItemStack newItem) {
        if (newItem == null) {
            newItem = new ItemStack(Material.AIR);
        }

        newItem = newItem.clone();
        newItem.setAmount(1);

        setData(MetaIndex.FIREWORK_ITEM, newItem);
        sendData(MetaIndex.FIREWORK_ITEM);
    }

    public void setAttachedEntity(int entityId) {
        setData(MetaIndex.FIREWORK_ATTACHED_ENTITY, entityId);
        sendData(MetaIndex.FIREWORK_ATTACHED_ENTITY);
    }

    public int getAttachedEntity() {
        return getData(MetaIndex.FIREWORK_ATTACHED_ENTITY);
    }
}
