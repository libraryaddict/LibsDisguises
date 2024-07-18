package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.entity.Parrot;

public class ParrotWatcher extends TameableWatcher {
    public ParrotWatcher(Disguise disguise) {
        super(disguise);
    }

    public Parrot.Variant getVariant() {
        return getData(MetaIndex.PARROT_VARIANT);
    }

    public void setVariant(Parrot.Variant variant) {
        sendData(MetaIndex.PARROT_VARIANT, variant);
    }
}
