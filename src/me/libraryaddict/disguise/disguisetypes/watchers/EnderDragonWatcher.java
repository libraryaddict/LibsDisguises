package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

/**
 * @author Navid
 */
public class EnderDragonWatcher extends InsentientWatcher
{

    public EnderDragonWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public int getPhase()
    {
        return getData(FlagType.ENDERD_RAGON_PHASE);
    }

    public void setPhase(int phase)
    {
        setData(FlagType.ENDERD_RAGON_PHASE, phase);
        sendData(FlagType.ENDERD_RAGON_PHASE);
    }
}
