package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class ModdedWatcher extends FlagWatcher {
    public ModdedWatcher(Disguise disguise) {
        super(disguise);
    }

    /**
     * @param index
     * @param object
     */
    public void setMetadata(int index, Object object) {
        getEntityValues().put(index, object);
    }

    public Object getMetadata(int index) {
        return getEntityValues().get(index);
    }
}
