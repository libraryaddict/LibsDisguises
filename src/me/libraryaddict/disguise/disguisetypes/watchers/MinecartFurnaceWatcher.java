package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * Created by libraryaddict on 6/08/2018.
 */
public class MinecartFurnaceWatcher extends MinecartWatcher {
    public MinecartFurnaceWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isFueled() {
        return getData(MetaIndex.MINECART_FURANCE_FUELED);
    }

    public void setFueled(boolean fueled) {
        setData(MetaIndex.MINECART_FURANCE_FUELED, fueled);
        sendData(MetaIndex.MINECART_FURANCE_FUELED);
    }
}
