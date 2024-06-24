package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import org.bukkit.entity.Axolotl;

/**
 * Created by libraryaddict on 15/06/2021.
 */
public class AxolotlWatcher extends AgeableWatcher {
    public AxolotlWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isPlayingDead() {
        return getData(MetaIndex.AXOLOTL_PLAYING_DEAD);
    }

    @MethodDescription("Is this Axolotl playing dead?")
    public void setPlayingDead(boolean playingDead) {
        setData(MetaIndex.AXOLOTL_PLAYING_DEAD, playingDead);
        sendData(MetaIndex.AXOLOTL_PLAYING_DEAD);
    }

    public Axolotl.Variant getVariant() {
        return getData(MetaIndex.AXOLOTL_VARIANT);
    }

    @MethodDescription("What variant of Axolotl is this?")
    public void setVariant(Axolotl.Variant variant) {
        setData(MetaIndex.AXOLOTL_VARIANT, variant);
        sendData(MetaIndex.AXOLOTL_VARIANT);
    }
}
