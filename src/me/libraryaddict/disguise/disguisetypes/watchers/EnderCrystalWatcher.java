package me.libraryaddict.disguise.disguisetypes.watchers;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.google.common.base.Optional;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

/**
 * @author Navid
 */
public class EnderCrystalWatcher extends FlagWatcher
{
    public EnderCrystalWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public void setBeamTarget(BlockPosition position)
    {
        setValue(5, Optional.of(position));
        sendData(5);
    }

    public Optional<BlockPosition> getBeamTarget()
    {
        return (Optional) getValue(5, Optional.absent());
    }

    public void setShowBottom(boolean bool)
    {
        setValue(6, bool);
        sendData(6);
    }

    public boolean isShowBottom()
    {
        return (boolean) getValue(6, false);
    }

}
