package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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

    public void setFirework(ItemStack newItem) {
        if (newItem == null) {
            newItem = new ItemStack(Material.AIR);
        }

        newItem = newItem.clone();
        newItem.setAmount(1);

        setData(MetaIndex.FIREWORK_ITEM, newItem);
        sendData(MetaIndex.FIREWORK_ITEM);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public boolean isShotAtAngle() {
        return getData(MetaIndex.FIREWORK_SHOT_AT_ANGLE);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setShotAtAngle(boolean shotAtAngle) {
        setData(MetaIndex.FIREWORK_SHOT_AT_ANGLE, shotAtAngle);
        sendData(MetaIndex.FIREWORK_SHOT_AT_ANGLE);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public OptionalInt getAttachedEntityOpt() {
        return getData(MetaIndex.FIREWORK_ATTACHED_ENTITY);
    }

    public int getAttachedEntity() {
        return getData(MetaIndex.FIREWORK_ATTACHED_ENTITY).orElse(0);
    }

    public void setAttachedEntity(int entityId) {
        setAttachedEntity(entityId == 0 ? OptionalInt.empty() : OptionalInt.of(entityId));
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setAttachedEntity(OptionalInt entityId) {
        if (NmsVersion.v1_14.isSupported()) {
            setData(MetaIndex.FIREWORK_ATTACHED_ENTITY, entityId);
            sendData(MetaIndex.FIREWORK_ATTACHED_ENTITY);
        } else {
            setData(MetaIndex.FIREWORK_ATTACHED_ENTITY_OLD, entityId.orElse(0));
            sendData(MetaIndex.FIREWORK_ATTACHED_ENTITY_OLD);
        }
    }
}
