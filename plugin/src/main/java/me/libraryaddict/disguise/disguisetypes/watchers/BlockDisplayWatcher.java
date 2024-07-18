package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodMappedAs;
import org.bukkit.block.data.BlockData;
import org.joml.Vector3f;

public class BlockDisplayWatcher extends DisplayWatcher {
    public BlockDisplayWatcher(Disguise disguise) {
        super(disguise);

        // So we're not seeing air
        setBlockState(WrappedBlockState.getDefaultState(StateTypes.STONE));
        // So its centered
        setTranslation(new Vector3f(-0.5f, 0f, -0.5f));
    }

    @MethodMappedAs("getBlock")
    public WrappedBlockState getBlockState() {
        return getData(MetaIndex.BLOCK_DISPLAY_BLOCK_STATE);
    }

    @MethodDescription("What block can players see?")
    @MethodMappedAs("setBlock")
    public void setBlockState(WrappedBlockState block) {
        sendData(MetaIndex.BLOCK_DISPLAY_BLOCK_STATE, block);
    }

    public BlockData getBlock() {
        return SpigotConversionUtil.toBukkitBlockData(getData(MetaIndex.BLOCK_DISPLAY_BLOCK_STATE));
    }

    public void setBlock(BlockData block) {
        sendData(MetaIndex.BLOCK_DISPLAY_BLOCK_STATE, SpigotConversionUtil.fromBukkitBlockData(block));
    }
}
