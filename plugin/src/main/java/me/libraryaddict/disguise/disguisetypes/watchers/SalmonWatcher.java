package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.entity.Salmon;

public class SalmonWatcher extends FishWatcher {
    public SalmonWatcher(Disguise disguise) {
        super(disguise);
    }

    @NmsAddedIn(NmsVersion.v1_21_R2)
    public Salmon.Variant getVariant() {
        return getData(MetaIndex.SALMON_VARIANT);
    }

    @NmsAddedIn(NmsVersion.v1_21_R2)
    public void setVariant(Salmon.Variant variant) {
        sendData(MetaIndex.SALMON_VARIANT, variant);
    }
}
