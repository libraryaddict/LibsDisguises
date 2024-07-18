package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.entity.Frog;

public class FrogWatcher extends AgeableWatcher {
    public FrogWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            setVariant(ReflectionManager.randomEnum(Frog.Variant.class));
        }
    }

    public Frog.Variant getVariant() {
        return getData(MetaIndex.FROG_VARIANT);
    }

    @RandomDefaultValue
    public void setVariant(Frog.Variant variant) {
        sendData(MetaIndex.FROG_VARIANT, variant);
    }
}
