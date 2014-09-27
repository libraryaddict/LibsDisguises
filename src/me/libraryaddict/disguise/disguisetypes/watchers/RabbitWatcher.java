package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class RabbitWatcher extends AgeableWatcher {

    public RabbitWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setRabbitType(int rabbitType) {
        setValue(18, (byte) rabbitType);
        sendData(18);
    }

}
