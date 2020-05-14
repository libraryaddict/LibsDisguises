package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.RabbitType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;

public class RabbitWatcher extends AgeableWatcher {

    public RabbitWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            setType(RabbitType.values()[DisguiseUtilities.random.nextInt(RabbitType.values().length)]);
        }
    }

    public RabbitType getType() {
        return RabbitType.getType(getData(MetaIndex.RABBIT_TYPE));
    }

    @RandomDefaultValue
    public void setType(RabbitType type) {
        setData(MetaIndex.RABBIT_TYPE, type.getTypeId());
        sendData(MetaIndex.RABBIT_TYPE);
    }
}
