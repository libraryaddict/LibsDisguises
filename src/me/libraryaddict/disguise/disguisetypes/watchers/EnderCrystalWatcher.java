package me.libraryaddict.disguise.disguisetypes.watchers;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.google.common.base.Optional;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
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
        setData(MetaIndex.ENDER_CRYSTAL_BEAM, Optional.of(position));
        sendData(MetaIndex.ENDER_CRYSTAL_BEAM);
    }

    public Optional<BlockPosition> getBeamTarget()
    {
        return getData(MetaIndex.ENDER_CRYSTAL_BEAM);
    }

    public void setShowBottom(boolean bool)
    {
        setData(MetaIndex.ENDER_CRYSTAL_PLATE, bool);
        sendData(MetaIndex.ENDER_CRYSTAL_PLATE);
    }

    public boolean isShowBottom()
    {
        return getData(MetaIndex.ENDER_CRYSTAL_PLATE);
    }

}
