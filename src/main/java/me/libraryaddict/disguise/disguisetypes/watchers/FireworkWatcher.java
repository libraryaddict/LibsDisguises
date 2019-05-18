package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

import java.util.OptionalInt;

public class FireworkWatcher extends FlagWatcher {
    public FireworkWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getFirework() {
        if (getData(MetaIndex.FIREWORK_ITEM) == null) {
            return new ItemStack(Material.AIR);
        }

        return getData(MetaIndex.FIREWORK_ITEM);
    }

    public boolean isShotAtAngle() {
        return getData(MetaIndex.FIREWORK_SHOT_AT_ANGLE);
    }

    public void setShotAtAngle(boolean shotAtAngle) {
        setData(MetaIndex.FIREWORK_SHOT_AT_ANGLE, shotAtAngle);
        sendData(MetaIndex.FIREWORK_SHOT_AT_ANGLE);
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

    public void setAttachedEntity(OptionalInt entityId) {
        setData(MetaIndex.FIREWORK_ATTACHED_ENTITY, entityId);
        sendData(MetaIndex.FIREWORK_ATTACHED_ENTITY);
    }

    public OptionalInt getAttachedEntity() {
        return getData(MetaIndex.FIREWORK_ATTACHED_ENTITY);
    }
}
