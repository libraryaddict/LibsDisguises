package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;

/**
 * Created by libraryaddict on 6/08/2018.
 */
public class TridentWatcher extends ArrowWatcher {
    public TridentWatcher(Disguise disguise) {
        super(disguise);
    }

    @NmsAddedIn(NmsVersion.v1_15)
    public boolean isEnchanted() {
        return getData(MetaIndex.TRIDENT_ENCHANTED);
    }

    @NmsAddedIn(NmsVersion.v1_15)
    public void setEnchanted(boolean enchanted) {
        setData(MetaIndex.TRIDENT_ENCHANTED, enchanted);
        sendData(MetaIndex.TRIDENT_ENCHANTED);
    }
}
