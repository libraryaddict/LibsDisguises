package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import org.bukkit.entity.Axolotl;

public class AxolotlWatcher extends AgeableWatcher {
    public AxolotlWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            setVariant(ReflectionManager.randomEnum(Axolotl.Variant.class));
        }
    }

    public boolean isPlayingDead() {
        return getData(MetaIndex.AXOLOTL_PLAYING_DEAD);
    }

    @MethodDescription("Is this Axolotl playing dead?")
    public void setPlayingDead(boolean playingDead) {
        sendData(MetaIndex.AXOLOTL_PLAYING_DEAD, playingDead);
    }

    public Axolotl.Variant getVariant() {
        return getData(MetaIndex.AXOLOTL_VARIANT);
    }

    @RandomDefaultValue
    @MethodDescription("What variant of Axolotl is this?")
    public void setVariant(Axolotl.Variant variant) {
        sendData(MetaIndex.AXOLOTL_VARIANT, variant);
    }
}
