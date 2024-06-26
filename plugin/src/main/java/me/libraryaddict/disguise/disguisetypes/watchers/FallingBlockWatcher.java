package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodMappedAs;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FallingBlockWatcher extends FlagWatcher {
    private int blockCombinedId = 1;
    @Getter
    private boolean gridLocked;

    public FallingBlockWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    public FallingBlockWatcher clone(Disguise disguise) {
        FallingBlockWatcher watcher = (FallingBlockWatcher) super.clone(disguise);

        if (NmsVersion.v1_13.isSupported()) {
            watcher.setBlockData(getBlockData().clone());
        } else {
            watcher.setBlock(getBlock().clone());
        }

        return watcher;
    }

    public void setGridLocked(boolean gridLocked) {
        if (isGridLocked() == gridLocked) {
            return;
        }

        this.gridLocked = gridLocked;

        if (getDisguise().isDisguiseInUse() && getDisguise().getEntity() != null) {
            Location loc = getDisguise().getEntity().getLocation();
            double x = conRel(loc.getX(), loc.getBlockX() + 0.5);
            double y = conRel(loc.getY(), loc.getBlockY() + (loc.getY() % 1 >= 0.85 ? 1 : loc.getY() % 1 >= 0.35 ? .5 : 0));
            double z = conRel(loc.getZ(), loc.getBlockZ() + 0.5);

            for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
                int entityId =
                    getDisguise().getEntity() == player ? DisguiseAPI.getSelfDisguiseId() : getDisguise().getEntity().getEntityId();

                WrapperPlayServerEntityRelativeMove relMov = new WrapperPlayServerEntityRelativeMove(entityId, x, y, z, true);

                if (isGridLocked()) {
                    PacketEvents.getAPI().getPlayerManager().sendPacket(player, relMov);
                } else {
                    PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, relMov);
                }
            }
        }
    }

    private short conRel(double oldCord, double newCord) {
        return (short) (((oldCord - newCord) * 4096) * (isGridLocked() ? -1 : 1));
    }

    @Deprecated
    public ItemStack getBlock() {
        return ReflectionManager.getItemStackByCombinedId(getBlockCombinedId());
    }

    @Deprecated
    public void setBlock(ItemStack block) {
        if (block == null || block.getType() == null || block.getType() == Material.AIR || !block.getType().isBlock()) {
            block = new ItemStack(Material.STONE);
        }

        this.blockCombinedId = ReflectionManager.getCombinedIdByItemStack(block);

        if (!getDisguise().isCustomDisguiseName()) {
            getDisguise().setDisguiseName(TranslateType.DISGUISE_OPTIONS_PARAMETERS.get("Block") + " " +
                TranslateType.DISGUISE_OPTIONS_PARAMETERS.get(ReflectionManager.toReadable(block.getType().name(), " ")));
        }

        if (DisguiseAPI.isDisguiseInUse(getDisguise()) && getDisguise().getWatcher() == this) {
            DisguiseUtilities.refreshTrackers(getDisguise());
        }
    }

    @MethodMappedAs("getBlock")
    public WrappedBlockState getBlockState() {
        return WrappedBlockState.getByGlobalId(getBlockCombinedId());
    }

    @MethodMappedAs("setBlock")
    public void setBlockState(WrappedBlockState state) {
        setBlockCombinedId(state.getType().getName(), state.getGlobalId());
    }

    @NmsAddedIn(NmsVersion.v1_13)
    public BlockData getBlockData() {
        return ReflectionManager.getBlockDataByCombinedId(getBlockCombinedId());
    }

    @NmsAddedIn(NmsVersion.v1_13)
    public void setBlockData(BlockData data) {
        if (data == null || data.getMaterial() == Material.AIR && data.getMaterial().isBlock()) {
            setBlock(null);
            return;
        }

        setBlockCombinedId(data.getMaterial().name(), ReflectionManager.getCombinedIdByBlockData(data));
    }

    private void setBlockCombinedId(String materialName, int combinedId) {
        this.blockCombinedId = combinedId;

        if (!getDisguise().isCustomDisguiseName()) {
            getDisguise().setDisguiseName(TranslateType.DISGUISE_OPTIONS_PARAMETERS.get("Block") + " " +
                TranslateType.DISGUISE_OPTIONS_PARAMETERS.get(ReflectionManager.toReadable(materialName, " ")));
        }

        if (DisguiseAPI.isDisguiseInUse(getDisguise()) && getDisguise().getWatcher() == this) {
            DisguiseUtilities.refreshTrackers(getDisguise());
        }
    }

    public int getBlockCombinedId() {
        if (blockCombinedId < 1) {
            blockCombinedId = 1;
        }

        return blockCombinedId;
    }
}
