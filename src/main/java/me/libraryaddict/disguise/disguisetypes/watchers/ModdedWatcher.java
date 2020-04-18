package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

/**
 * Created by libraryaddict on 13/04/2020.
 */
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
