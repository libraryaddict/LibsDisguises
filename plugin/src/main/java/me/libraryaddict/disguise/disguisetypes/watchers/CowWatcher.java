package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.entity.Cow;

public class CowWatcher extends AgeableWatcher {
    public CowWatcher(Disguise disguise) {
        super(disguise);
    }

    @NmsAddedIn(NmsVersion.v1_21_R4)
    public void setVariant(Cow.Variant variant) {
        sendData(MetaIndex.COW_VARIANT, variant);
    }

    @NmsAddedIn(NmsVersion.v1_21_R4)
    public Cow.Variant getVariant() {
        return getData(MetaIndex.COW_VARIANT);
    }
}
