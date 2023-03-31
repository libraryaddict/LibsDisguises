package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.GolemCrack;
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
        if (cracks == getCracks() || cracks == null) {
            return;
        }

        this.cracks = cracks;

        switch (cracks) {
            case HEALTH_25:
                setHealth(24);
                break;
            case HEALTH_50:
                setHealth(49);
                break;
            case HEALTH_75:
                setHealth(74);
                break;
            case HEALTH_100:
                setHealth(100);
                break;
        }

        if (!isMaxHealthSet() || getMaxHealth() != 100) {
            setMaxHealth(100);
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
