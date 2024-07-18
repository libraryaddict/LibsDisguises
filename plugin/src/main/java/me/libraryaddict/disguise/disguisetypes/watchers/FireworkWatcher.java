package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

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

        sendData(MetaIndex.FIREWORK_ITEM, newItem);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public boolean isShotAtAngle() {
        return getData(MetaIndex.FIREWORK_SHOT_AT_ANGLE);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setShotAtAngle(boolean shotAtAngle) {
        sendData(MetaIndex.FIREWORK_SHOT_AT_ANGLE, shotAtAngle);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public Optional<Integer> getAttachedEntityOpt() {
        return getData(MetaIndex.FIREWORK_ATTACHED_ENTITY);
    }

    public int getAttachedEntity() {
        return getData(MetaIndex.FIREWORK_ATTACHED_ENTITY).orElse(0);
    }

    public void setAttachedEntity(int entityId) {
        setAttachedEntity(entityId == 0 ? Optional.empty() : Optional.of(entityId));
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setAttachedEntity(Optional<Integer> entityId) {
        if (NmsVersion.v1_14.isSupported()) {
            sendData(MetaIndex.FIREWORK_ATTACHED_ENTITY, entityId);
        } else {
            sendData(MetaIndex.FIREWORK_ATTACHED_ENTITY_OLD, entityId.orElse(0));
        }
    }
}
