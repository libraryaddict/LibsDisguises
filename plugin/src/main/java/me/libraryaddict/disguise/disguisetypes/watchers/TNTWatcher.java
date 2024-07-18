package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodMappedAs;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.block.data.BlockData;

public class TNTWatcher extends FlagWatcher {
    public TNTWatcher(Disguise disguise) {
        super(disguise);
    }

    @NmsAddedIn(NmsVersion.v1_20_R3)
    @MethodMappedAs("getBlock")
    public WrappedBlockState getBlockData() {
        return getData(MetaIndex.TNT_BLOCK_TYPE);
    }

    @NmsAddedIn(NmsVersion.v1_20_R3)
    @MethodMappedAs("setBlock")
    public void setBlockData(WrappedBlockState block) {
        sendData(MetaIndex.TNT_BLOCK_TYPE, block);
    }

    @NmsAddedIn(NmsVersion.v1_20_R3)
    public BlockData getBlock() {
        return SpigotConversionUtil.toBukkitBlockData(getData(MetaIndex.TNT_BLOCK_TYPE));
    }

    @NmsAddedIn(NmsVersion.v1_20_R3)
    public void setBlock(BlockData block) {
        sendData(MetaIndex.TNT_BLOCK_TYPE, SpigotConversionUtil.fromBukkitBlockData(block));
    }
}
