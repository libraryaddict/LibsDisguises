package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.entity.MushroomCow;

public class MushroomCowWatcher extends AgeableWatcher {
    public MushroomCowWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            setVariant(ReflectionManager.randomEnum(MushroomCow.Variant.class));
        }
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public MushroomCow.Variant getVariant() {
        return getData(MetaIndex.MUSHROOM_COW_TYPE);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setVariant(MushroomCow.Variant variant) {
        sendData(MetaIndex.MUSHROOM_COW_TYPE, variant);
    }
}
