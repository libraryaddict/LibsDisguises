package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.block.data.BlockData;

public class TNTWatcher extends FlagWatcher {
    public TNTWatcher(Disguise disguise) {
        super(disguise);
    }

    public BlockData getBlock() {
        return getData(MetaIndex.TNT_BLOCK_TYPE);
    }

    @NmsAddedIn(NmsVersion.v1_20_R3)
    public void setBlock(BlockData block) {
        setData(MetaIndex.TNT_BLOCK_TYPE, block);
        sendData(MetaIndex.TNT_BLOCK_TYPE);
    }
}
