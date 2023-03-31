package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.block.data.BlockData;

public class BlockDisplayWatcher extends DisplayWatcher {
    public BlockDisplayWatcher(Disguise disguise) {
        super(disguise);
    }

    public BlockData getBlock() {
        return getData(MetaIndex.BLOCK_DISPLAY_BLOCK_STATE);
    }

    public void setBlock(BlockData block) {
        setData(MetaIndex.BLOCK_DISPLAY_BLOCK_STATE, block);
        sendData(MetaIndex.BLOCK_DISPLAY_BLOCK_STATE);
    }
}
