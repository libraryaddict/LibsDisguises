package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.inventory.ItemStack;

import com.google.common.base.Optional;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

public class SplashPotionWatcher extends FlagWatcher
{
    private int potionId;

    public SplashPotionWatcher(Disguise disguise)
    {
        super(disguise);
    }

    @Override
    public SplashPotionWatcher clone(Disguise disguise)
    {
        SplashPotionWatcher watcher = (SplashPotionWatcher) super.clone(disguise);
        watcher.setPotionId(getPotionId());

        return watcher;
    }

    public int getPotionId()
    {
        return potionId;
    }

    public void setSplashPotion(ItemStack item)
    {
        setValue(FlagType.SPLASH_POTION_ITEM, Optional.of(item));
        sendData(FlagType.SPLASH_POTION_ITEM);
    }

    public ItemStack getSplashPotion()
    {
        return getValue(FlagType.SPLASH_POTION_ITEM).get();
    }

    public void setPotionId(int newPotionId)
    {
        this.potionId = newPotionId;

        if (getDisguise().getEntity() != null && getDisguise().getWatcher() == this)
        {
            DisguiseUtilities.refreshTrackers(getDisguise());
        }
    }

}
