package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.inventory.ItemStack;

public class SplashPotionWatcher extends FlagWatcher {
    private int potionId;

    public SplashPotionWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    public SplashPotionWatcher clone(Disguise disguise) {
        SplashPotionWatcher watcher = (SplashPotionWatcher) super.clone(disguise);
        watcher.setPotionId(getPotionId());

        return watcher;
    }

    public int getPotionId() {
        return potionId;
    }

    public void setPotionId(int newPotionId) {
        this.potionId = newPotionId;

        if (getDisguise().getEntity() != null && getDisguise().getWatcher() == this) {
            DisguiseUtilities.refreshTrackers(getDisguise());
        }
    }

    public ItemStack getSplashPotion() {
        return getData(MetaIndex.SPLASH_POTION_ITEM);
    }

    public void setSplashPotion(ItemStack item) {
        setData(MetaIndex.SPLASH_POTION_ITEM, item);
        sendData(MetaIndex.SPLASH_POTION_ITEM);
    }
}
