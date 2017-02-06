package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

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
        return getData(MetaIndex.ENDERD_RAGON_PHASE);
    }

    public void setPhase(int phase)
    {
        setData(MetaIndex.ENDERD_RAGON_PHASE, phase);
        sendData(MetaIndex.ENDERD_RAGON_PHASE);
    }
}
