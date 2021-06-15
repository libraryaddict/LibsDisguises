package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
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

    public void setPlayingDead(boolean playingDead) {
        setData(MetaIndex.AXOLOTL_PLAYING_DEAD, playingDead);
        sendData(MetaIndex.AXOLOTL_PLAYING_DEAD);
    }

    public Axolotl.Variant getVarient() {
        return Axolotl.Variant.values()[getData(MetaIndex.AXOLOTL_VARIENT)];
    }

    public void setVarient(Axolotl.Variant varient) {
        setData(MetaIndex.AXOLOTL_VARIENT, varient.ordinal());
        sendData(MetaIndex.AXOLOTL_VARIENT);
    }
}
