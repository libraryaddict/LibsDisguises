package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.entity.Parrot;

/**
 * Created by libraryaddict on 9/06/2017.
 */
public class ParrotWatcher extends TameableWatcher {
    public ParrotWatcher(Disguise disguise) {
        super(disguise);
    }

    public Parrot.Variant getVariant() {
        return Parrot.Variant.values()[getData(MetaIndex.PARROT_VARIANT)];
    }

    public void setVariant(Parrot.Variant variant) {
        setData(MetaIndex.PARROT_VARIANT, variant.ordinal());
        sendData(MetaIndex.PARROT_VARIANT);
    }
}
