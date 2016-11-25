package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.disguisetypes.RabbitType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

public class RabbitWatcher extends AgeableWatcher
{

    public RabbitWatcher(Disguise disguise)
    {
        super(disguise);
        setType(RabbitType.values()[DisguiseUtilities.random.nextInt(RabbitType.values().length)]);
    }

    public RabbitType getType()
    {
        return RabbitType.getType((int) getData(FlagType.RABBIT_TYPE));
    }

    public void setType(RabbitType type)
    {
        setData(FlagType.RABBIT_TYPE, type.getTypeId());
        sendData(FlagType.RABBIT_TYPE);
    }

}
