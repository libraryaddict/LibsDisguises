package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.util.Vector3i;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;

import java.util.Optional;

/**
 * @author Navid
 */
public class EnderCrystalWatcher extends FlagWatcher {
    public EnderCrystalWatcher(Disguise disguise) {
        super(disguise);
    }

    public Vector3i getBeamTarget() {
        return getData(MetaIndex.ENDER_CRYSTAL_BEAM).orElse(null);
    }

    public void setBeamTarget(Vector3i position) {
        sendData(MetaIndex.ENDER_CRYSTAL_BEAM, position == null ? Optional.empty() : Optional.of(position));
    }

    public boolean isShowBottom() {
        return getData(MetaIndex.ENDER_CRYSTAL_PLATE);
    }

    @MethodDescription("Can you see the Ender Crystal's base plate?")
    public void setShowBottom(boolean bool) {
        sendData(MetaIndex.ENDER_CRYSTAL_PLATE, bool);
    }
}
