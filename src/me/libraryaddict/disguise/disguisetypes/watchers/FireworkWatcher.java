package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class FireworkWatcher extends FlagWatcher {
    public FireworkWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getFirework() {
        if (getData(FlagType.FIREWORK_ITEM) == null) {
            return new ItemStack(Material.AIR);
        }

        return (ItemStack) getData(FlagType.FIREWORK_ITEM);
    }

    public void setFirework(ItemStack newItem) {
        if (newItem == null) {
            newItem = new ItemStack(Material.AIR);
        }

        newItem = newItem.clone();
        newItem.setAmount(1);

        setData(FlagType.FIREWORK_ITEM, newItem);
        sendData(FlagType.FIREWORK_ITEM);
    }

    public void setAttachedEntity(int entityId) {
        setData(FlagType.FIREWORK_ATTACHED_ENTITY, entityId);
        sendData(FlagType.FIREWORK_ATTACHED_ENTITY);
    }

    public int getAttachedEntity() {
        return getData(FlagType.FIREWORK_ATTACHED_ENTITY);
    }
}
