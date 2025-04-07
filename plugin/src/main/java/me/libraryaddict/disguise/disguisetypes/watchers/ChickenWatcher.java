package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.entity.Chicken;

public class ChickenWatcher extends AgeableWatcher {
    public ChickenWatcher(Disguise disguise) {
        super(disguise);
    }

    @NmsAddedIn(NmsVersion.v1_21_R4)
    public void setVariant(Chicken.Variant variant) {
        sendData(MetaIndex.CHICKEN_VARIANT, variant);
    }

    @NmsAddedIn(NmsVersion.v1_21_R4)
    public Chicken.Variant getVariant() {
        return getData(MetaIndex.CHICKEN_VARIANT);
    }
}
