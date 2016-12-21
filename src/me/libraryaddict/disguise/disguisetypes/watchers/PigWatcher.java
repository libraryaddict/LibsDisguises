package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class PigWatcher extends AgeableWatcher {

    public PigWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isSaddled() {
        return (boolean) getData(FlagType.PIG_SADDLED);
    }

    public void setSaddled(boolean isSaddled) {
        setData(FlagType.PIG_SADDLED, isSaddled);
        sendData(FlagType.PIG_SADDLED);
    }

    /* public int getUnknown() {
        return getData(FlagType.PIG_UNKNOWN);
    }
    
    public void setUnknown(int unknown) {
        setData(FlagType.PIG_UNKNOWN, unknown);
        sendData(FlagType.PIG_UNKNOWN);
    }*/
}
