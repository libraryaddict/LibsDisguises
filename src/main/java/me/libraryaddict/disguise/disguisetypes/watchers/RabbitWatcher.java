package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.RabbitType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

public class RabbitWatcher extends AgeableWatcher {

    public RabbitWatcher(Disguise disguise) {
        super(disguise);
        setType(RabbitType.values()[DisguiseUtilities.random.nextInt(RabbitType.values().length)]);
    }

    public RabbitType getType() {
        return RabbitType.getType((int) getValue(18, 0));
    }

    public void setType(RabbitType type) {
        setValue(12, type.getTypeId());
        sendData(12);
    }

}
