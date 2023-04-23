package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.joml.Vector3f;

public class BlockDisplayWatcher extends DisplayWatcher {
    public BlockDisplayWatcher(Disguise disguise) {
        super(disguise);

        // So we're not seeing air
        setBlock(Bukkit.createBlockData(Material.STONE));
        // So its centered
        setTranslation(new Vector3f(-0.5f, 0f, -0.5f));
    }

    public BlockData getBlock() {
        return getData(MetaIndex.BLOCK_DISPLAY_BLOCK_STATE);
    }

    public void setBlock(BlockData block) {
        setData(MetaIndex.BLOCK_DISPLAY_BLOCK_STATE, block);
        sendData(MetaIndex.BLOCK_DISPLAY_BLOCK_STATE);
    }
}
