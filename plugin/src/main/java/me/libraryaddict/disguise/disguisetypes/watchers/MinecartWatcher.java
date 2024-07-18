package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

public class MinecartWatcher extends FlagWatcher {

    public MinecartWatcher(Disguise disguise) {
        super(disguise);
    }

    @Deprecated
    public ItemStack getBlockInCart() {
        if (!hasValue(MetaIndex.MINECART_BLOCK)) {
            return new ItemStack(Material.AIR);
        }

        return ReflectionManager.getItemStackByCombinedId(getData(MetaIndex.MINECART_BLOCK));
    }

    @Deprecated
    public void setBlockInCart(ItemStack item) {
        setData(MetaIndex.MINECART_BLOCK, ReflectionManager.getCombinedIdByItemStack(item));
        setData(MetaIndex.MINECART_BLOCK_VISIBLE, item != null && item.getType() != Material.AIR);

        sendData(MetaIndex.MINECART_BLOCK, MetaIndex.MINECART_BLOCK_VISIBLE);
    }

    public WrappedBlockState getBlock() {
        return WrappedBlockState.getByGlobalId(getData(MetaIndex.MINECART_BLOCK));
    }

    public void setBlock(WrappedBlockState state) {
        setData(MetaIndex.MINECART_BLOCK, state == null || state.getType() == StateTypes.AIR ? 0 : state.getGlobalId());
        setData(MetaIndex.MINECART_BLOCK_VISIBLE, state != null && state.getType() != StateTypes.AIR);

        sendData(MetaIndex.MINECART_BLOCK, MetaIndex.MINECART_BLOCK_VISIBLE);
    }

    @NmsAddedIn(NmsVersion.v1_13)
    @Deprecated
    public BlockData getBlockData() {
        return ReflectionManager.getBlockDataByCombinedId(getData(MetaIndex.MINECART_BLOCK));
    }

    @NmsAddedIn(NmsVersion.v1_13)
    @Deprecated
    public void setBlockData(BlockData data) {
        setData(MetaIndex.MINECART_BLOCK, ReflectionManager.getCombinedIdByBlockData(data));
        setData(MetaIndex.MINECART_BLOCK_VISIBLE, data != null && data.getMaterial() != Material.AIR);

        sendData(MetaIndex.MINECART_BLOCK, MetaIndex.MINECART_BLOCK_VISIBLE);
    }

    @Deprecated
    public int getBlockYOffset() {
        return getData(MetaIndex.MINECART_BLOCK_Y);
    }

    public int getBlockOffset() {
        return getData(MetaIndex.MINECART_BLOCK_Y);
    }

    public void setBlockOffset(int i) {
        sendData(MetaIndex.MINECART_BLOCK_Y, i);
    }

    public boolean isViewBlockInCart() {
        return getData(MetaIndex.MINECART_BLOCK_VISIBLE);
    }

    public void setViewBlockInCart(boolean viewBlock) {
        sendData(MetaIndex.MINECART_BLOCK_VISIBLE, viewBlock);
    }
}
