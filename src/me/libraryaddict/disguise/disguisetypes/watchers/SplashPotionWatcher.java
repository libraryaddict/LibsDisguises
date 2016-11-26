package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

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

    public void setSplashPotion(ItemStack item) {
        setData(FlagType.SPLASH_POTION_ITEM, item);
        sendData(FlagType.SPLASH_POTION_ITEM);
    }

    public ItemStack getSplashPotion() {
        return getData(FlagType.SPLASH_POTION_ITEM);
    }

    public void setPotionId(int newPotionId) {
        this.potionId = newPotionId;

        if (getDisguise().getEntity() != null && getDisguise().getWatcher() == this) {
            DisguiseUtilities.refreshTrackers(getDisguise());
        }
    }

}
