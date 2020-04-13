package me.libraryaddict.disguise.disguisetypes.watchers;

import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * Created by libraryaddict on 13/04/2020.
 */
public class CustomWatcher extends FlagWatcher {
    @Getter
    private DisguiseType inherits;
    @Getter
    @Setter
    private int typeId;

    public CustomWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setInherits(DisguiseType toClone) {
        this.inherits = toClone;
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
