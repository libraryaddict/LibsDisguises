package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

public class SlimeWatcher extends InsentientWatcher
{

    public SlimeWatcher(Disguise disguise)
    {
        super(disguise);
        setSize(DisguiseUtilities.random.nextInt(4) + 1);
    }

    public int getSize()
    {
        return (int) getData(MetaIndex.SLIME_SIZE);
    }

    public void setSize(int size)
    {
        if (size <= 0 || size >= 128)
        {
            size = 1;
        }

        setData(MetaIndex.SLIME_SIZE, size);
        sendData(MetaIndex.SLIME_SIZE);
    }

}
