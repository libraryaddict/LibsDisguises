package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import org.bukkit.entity.Frog;

import java.util.Random;

public class FrogWatcher extends AgeableWatcher {
    public FrogWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            setVariant(Frog.Variant.values()[new Random().nextInt(Frog.Variant.values().length)]);
        }
    }

    public Frog.Variant getVariant() {
        return getData(MetaIndex.FROG_VARIANT);
    }

    @RandomDefaultValue
    public void setVariant(Frog.Variant variant) {
        setData(MetaIndex.FROG_VARIANT, variant);
        sendData(MetaIndex.FROG_VARIANT);
    }
}
