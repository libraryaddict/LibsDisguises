package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

public class SplashPotionWatcher extends FlagWatcher {
    private int potionId;

    public SplashPotionWatcher(Disguise disguise) {
        super(disguise);
    }

    public int getPotionId() {
        return potionId;
    }

    @Override
    public SplashPotionWatcher clone(Disguise disguise) {
        SplashPotionWatcher watcher = (SplashPotionWatcher) super.clone(disguise);
        watcher.setPotionId(getPotionId());
        return watcher;
    }

    public void setPotionId(int newPotionId) {
        this.potionId = newPotionId;
        if (getDisguise().getEntity() != null && getDisguise().getWatcher() == this) {
            DisguiseUtilities.refreshTrackers(getDisguise().getEntity());
        }
    }

}
