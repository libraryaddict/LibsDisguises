package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.entity.MushroomCow;

/**
 * Created by libraryaddict on 6/05/2019.
 */
public class MushroomCowWatcher extends AgeableWatcher {
    public MushroomCowWatcher(Disguise disguise) {
        super(disguise);
    }

    public MushroomCow.Variant getVariant() {
        return MushroomCow.Variant.valueOf(getData(MetaIndex.MUSHROOM_COW_TYPE).toUpperCase());
    }

    public void setVariant(MushroomCow.Variant variant) {
        setData(MetaIndex.MUSHROOM_COW_TYPE, variant.name().toLowerCase());
        sendData(MetaIndex.MUSHROOM_COW_TYPE);
    }
}
