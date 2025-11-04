package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

public class MinecartWatcher extends FlagWatcher {
    public MinecartWatcher(Disguise disguise) {
        super(disguise);
    }

    @Deprecated
    public ItemStack getBlockInCart() {
        if (NmsVersion.v1_21_R4.isSupported()) {
            return ReflectionManager.getItemStackByCombinedId(ReflectionManager.getCombinedIdByWrappedBlockState(getBlock()));
        }

        if (!hasValue(MetaIndex.MINECART_BLOCK)) {
            return new ItemStack(Material.AIR);
        }

        return ReflectionManager.getItemStackByCombinedId(getData(MetaIndex.MINECART_BLOCK));
    }

    @Deprecated
    public void setBlockInCart(ItemStack item) {
        if (item != null && (item.getType() == null || !item.getType().isBlock())) {
            item = null;
        }

        if (NmsVersion.v1_21_R4.isSupported()) {
            int id = 0;

            if (item != null && item.getType() != Material.AIR) {
                id = ReflectionManager.getCombinedIdByItemStack(item);
            }

            sendData(MetaIndex.MINECART_BLOCK_NEW, item != null ? ReflectionManager.getWrappedBlockStateByCombinedId(id) : null);
            return;
        }

        setData(MetaIndex.MINECART_BLOCK, item != null ? ReflectionManager.getCombinedIdByItemStack(item) : null);
        setData(MetaIndex.MINECART_BLOCK_VISIBLE, item != null ? item.getType() != Material.AIR : null);
        sendData(MetaIndex.MINECART_BLOCK, MetaIndex.MINECART_BLOCK_VISIBLE);
    }

    public WrappedBlockState getBlock() {
        if (NmsVersion.v1_21_R4.isSupported()) {
            return getData(MetaIndex.MINECART_BLOCK_NEW);
        }

        return ReflectionManager.getWrappedBlockStateByCombinedId(getData(MetaIndex.MINECART_BLOCK));
    }

    public void setBlock(WrappedBlockState state) {
        if (NmsVersion.v1_21_R4.isSupported()) {
            sendData(MetaIndex.MINECART_BLOCK_NEW, state);
            return;
        }

        setData(MetaIndex.MINECART_BLOCK,
            state != null ? (state.getType() == StateTypes.AIR ? 0 : ReflectionManager.getCombinedIdByWrappedBlockState(state)) : null);
        setData(MetaIndex.MINECART_BLOCK_VISIBLE, state != null ? state.getGlobalId() != 0 && state.getType() != StateTypes.AIR : null);

        sendData(MetaIndex.MINECART_BLOCK, MetaIndex.MINECART_BLOCK_VISIBLE);
    }

    @NmsAddedIn(NmsVersion.v1_13)
    @Deprecated
    public BlockData getBlockData() {
        if (NmsVersion.v1_21_R4.isSupported()) {
            return ReflectionManager.getBlockDataByCombinedId(
                ReflectionManager.getCombinedIdByWrappedBlockState(getData(MetaIndex.MINECART_BLOCK_NEW)));
        }

        return ReflectionManager.getBlockDataByCombinedId(getData(MetaIndex.MINECART_BLOCK));
    }

    @NmsAddedIn(NmsVersion.v1_13)
    @Deprecated
    public void setBlockData(BlockData data) {
        if (data == null || !data.getMaterial().isBlock()) {
            setBlock(null);
            return;
        }

        if (data.getMaterial() == Material.AIR) {
            setBlock(WrappedBlockState.getByGlobalId(0));
            return;
        }

        int combinedId = ReflectionManager.getCombinedIdByBlockData(data);

        if (NmsVersion.v1_21_R4.isSupported()) {
            sendData(MetaIndex.MINECART_BLOCK_NEW, ReflectionManager.getWrappedBlockStateByCombinedId(combinedId));
            return;
        }

        setData(MetaIndex.MINECART_BLOCK, combinedId);
        setData(MetaIndex.MINECART_BLOCK_VISIBLE, true);
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

    @NmsRemovedIn(NmsVersion.v1_21_R4)
    public boolean isViewBlockInCart() {
        return getData(MetaIndex.MINECART_BLOCK_VISIBLE);
    }

    @NmsRemovedIn(NmsVersion.v1_21_R4)
    public void setViewBlockInCart(boolean viewBlock) {
        sendData(MetaIndex.MINECART_BLOCK_VISIBLE, viewBlock);
    }
}
