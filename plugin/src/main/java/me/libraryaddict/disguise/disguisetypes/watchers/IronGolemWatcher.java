package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.GolemCrack;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;

public class IronGolemWatcher extends InsentientWatcher {
    private GolemCrack cracks;

    public IronGolemWatcher(Disguise disguise) {
        super(disguise);
    }

    @NmsAddedIn(NmsVersion.v1_16)
    public GolemCrack getCracks() {
        return cracks;
    }

    @NmsAddedIn(NmsVersion.v1_16)
    public void setCracks(GolemCrack cracks) {
        if (cracks == getCracks()) {
            return;
        }

        this.cracks = cracks;

        if (cracks == null) {
            sendData(MetaIndex.LIVING_HEALTH, null);

            if (isMaxHealthSet() && getMaxHealth() == 100) {
                // Resets the health, but will require the max health attribute to be sent
                setMaxHealth(-1);
            }
        } else {
            setHealth(cracks.getHealthShown());

            if (!isMaxHealthSet() || getMaxHealth() != 100) {
                setMaxHealth(100);
            }
        }
    }

    @Override
    public IronGolemWatcher clone(Disguise disguise) {
        IronGolemWatcher watcher = (IronGolemWatcher) super.clone(disguise);

        if (NmsVersion.v1_16.isSupported()) {
            watcher.setCracks(getCracks());
        }

        return watcher;
    }
}
