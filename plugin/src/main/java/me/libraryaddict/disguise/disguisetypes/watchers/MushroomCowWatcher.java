package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.entity.MushroomCow;

import java.util.Locale;

/**
 * Created by libraryaddict on 6/05/2019.
 */
public class MushroomCowWatcher extends AgeableWatcher {
    public MushroomCowWatcher(Disguise disguise) {
        super(disguise);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public MushroomCow.Variant getVariant() {
        return MushroomCow.Variant.valueOf(getData(MetaIndex.MUSHROOM_COW_TYPE).toUpperCase(Locale.ENGLISH));
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setVariant(MushroomCow.Variant variant) {
        setData(MetaIndex.MUSHROOM_COW_TYPE, variant.name().toLowerCase(Locale.ENGLISH));
        sendData(MetaIndex.MUSHROOM_COW_TYPE);
    }
}
