package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class MinecartFurnaceWatcher extends MinecartWatcher {
    public MinecartFurnaceWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isFueled() {
        return getData(MetaIndex.MINECART_FURANCE_FUELED);
    }

    public void setFueled(boolean fueled) {
        sendData(MetaIndex.MINECART_FURANCE_FUELED, fueled);
    }
}
