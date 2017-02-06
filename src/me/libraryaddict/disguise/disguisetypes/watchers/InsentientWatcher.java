package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.inventory.MainHand;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class InsentientWatcher extends LivingWatcher
{
    public InsentientWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public void setMainHand(MainHand mainHand)
    {
        setInsentientFlag(2, mainHand == MainHand.RIGHT);
        sendData(MetaIndex.INSENTIENT_META);
    }

    public MainHand getMainHand()
    {
        return getInsentientFlag(2) ? MainHand.RIGHT : MainHand.LEFT;
    }

    public boolean isAI()
    {
        return getInsentientFlag(1);
    }

    public void setAI(boolean ai)
    {
        setInsentientFlag(1, ai);
        sendData(MetaIndex.INSENTIENT_META);
    }

    private void setInsentientFlag(int i, boolean flag)
    {
        byte b0 = (byte) getData(MetaIndex.INSENTIENT_META);

        if (flag)
        {
            setData(MetaIndex.INSENTIENT_META, (byte) (b0 | 1 << i));
        }
        else
        {
            setData(MetaIndex.INSENTIENT_META, (byte) (b0 & (~1 << i)));
        }
    }

    private boolean getInsentientFlag(int i)
    {
        return ((byte) getData(MetaIndex.INSENTIENT_META) & 1 << i) != 0;
    }
}
