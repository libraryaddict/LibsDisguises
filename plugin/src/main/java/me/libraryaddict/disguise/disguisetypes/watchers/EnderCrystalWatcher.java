package me.libraryaddict.disguise.disguisetypes.watchers;

import com.comphenix.protocol.wrappers.BlockPosition;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

import java.util.Optional;

/**
 * @author Navid
 */
public class EnderCrystalWatcher extends FlagWatcher {
    public EnderCrystalWatcher(Disguise disguise) {
        super(disguise);
    }

    public BlockPosition getBeamTarget() {
        return getData(MetaIndex.ENDER_CRYSTAL_BEAM).orElse(null);
    }

    public void setBeamTarget(BlockPosition position) {
        setData(MetaIndex.ENDER_CRYSTAL_BEAM, position == null ? Optional.empty() : Optional.of(position));
        sendData(MetaIndex.ENDER_CRYSTAL_BEAM);
    }

    public boolean isShowBottom() {
        return getData(MetaIndex.ENDER_CRYSTAL_PLATE);
    }

    public void setShowBottom(boolean bool) {
        setData(MetaIndex.ENDER_CRYSTAL_PLATE, bool);
        sendData(MetaIndex.ENDER_CRYSTAL_PLATE);
    }
}
