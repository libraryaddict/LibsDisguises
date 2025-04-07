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

            sendData(MetaIndex.MINECART_BLOCK_NEW, ReflectionManager.getWrappedBlockStateByCombinedId(id));
            return;
        }

        setData(MetaIndex.MINECART_BLOCK, ReflectionManager.getCombinedIdByItemStack(item));

        if (!NmsVersion.v1_21_R4.isSupported()) {
            setData(MetaIndex.MINECART_BLOCK_VISIBLE, item != null && item.getType() != Material.AIR);
            sendData(MetaIndex.MINECART_BLOCK, MetaIndex.MINECART_BLOCK_VISIBLE);
        } else {
            sendData(MetaIndex.MINECART_BLOCK);
        }
    }

    public WrappedBlockState getBlock() {
        if (NmsVersion.v1_21_R4.isSupported()) {
            return getData(MetaIndex.MINECART_BLOCK_NEW);
        }

        return ReflectionManager.getWrappedBlockStateByCombinedId(getData(MetaIndex.MINECART_BLOCK));
    }

    public void setBlock(WrappedBlockState state) {
        if (state == null) {
            state = WrappedBlockState.getByGlobalId(0);
        }

        if (NmsVersion.v1_21_R4.isSupported()) {
            sendData(MetaIndex.MINECART_BLOCK_NEW, state);
            return;
        }

        setData(MetaIndex.MINECART_BLOCK,
            state.getType() == StateTypes.AIR ? 0 : ReflectionManager.getCombinedIdByWrappedBlockState(state));
        setData(MetaIndex.MINECART_BLOCK_VISIBLE, state.getGlobalId() != 0 && state.getType() != StateTypes.AIR);

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
        if (data == null || data.getMaterial() == Material.AIR || !data.getMaterial().isBlock()) {
            setBlock(WrappedBlockState.getByGlobalId(0));
            return;
        }

        if (NmsVersion.v1_21_R4.isSupported()) {
            sendData(MetaIndex.MINECART_BLOCK_NEW,
                ReflectionManager.getWrappedBlockStateByCombinedId(ReflectionManager.getCombinedIdByBlockData(data)));
            return;
        }

        setData(MetaIndex.MINECART_BLOCK, ReflectionManager.getCombinedIdByBlockData(data));

        if (!NmsVersion.v1_21_R4.isSupported()) {
            setData(MetaIndex.MINECART_BLOCK_VISIBLE, data != null && data.getMaterial() != Material.AIR);
            sendData(MetaIndex.MINECART_BLOCK, MetaIndex.MINECART_BLOCK_VISIBLE);
        } else {
            sendData(MetaIndex.MINECART_BLOCK);
        }
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
