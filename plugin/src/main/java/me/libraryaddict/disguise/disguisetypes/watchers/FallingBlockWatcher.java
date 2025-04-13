package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodMappedAs;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FallingBlockWatcher extends FlagWatcher implements GridLockedWatcher {
    private int blockCombinedId = 1;
    @Getter
    private boolean gridLocked;

    public FallingBlockWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    public FallingBlockWatcher clone(Disguise disguise) {
        FallingBlockWatcher watcher = (FallingBlockWatcher) super.clone(disguise);

        watcher.setBlockState(getBlockState().clone());

        return watcher;
    }

    public void setGridLocked(boolean gridLocked) {
        if (isGridLocked() == gridLocked) {
            return;
        }

        this.gridLocked = gridLocked;

        if (!getDisguise().isDisguiseInUse() || getDisguise().getEntity() == null) {
            return;
        }

        Location loc = getDisguise().getEntity().getLocation();
        double centerX = GridLockedWatcher.center(loc.getX(), getWidthX());
        double centerY = loc.getBlockY() + (loc.getY() % 1 >= 0.85 ? 1 : loc.getY() % 1 >= 0.35 ? .5 : 0);
        double centerZ = GridLockedWatcher.center(loc.getZ(), getWidthZ());

        double x = conRel(loc.getX(), centerX);
        double y = conRel(loc.getY(), centerY);
        double z = conRel(loc.getZ(), centerZ);

        for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
            int entityId = getDisguise().getEntity() == player ? DisguiseAPI.getSelfDisguiseId() : getDisguise().getEntity().getEntityId();

            WrapperPlayServerEntityRelativeMove relMov = new WrapperPlayServerEntityRelativeMove(entityId, x, y, z, true);

            if (isGridLocked()) {
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, relMov);
            } else {
                PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, relMov);
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

        DisguiseParser.updateDisguiseName(getDisguise());

        if (DisguiseAPI.isDisguiseInUse(getDisguise()) && getDisguise().getWatcher() == this) {
            DisguiseUtilities.refreshTrackers(getDisguise());
        }
    }

    @MethodMappedAs("getBlock")
    public WrappedBlockState getBlockState() {
        return ReflectionManager.getWrappedBlockStateByCombinedId(getBlockCombinedId());
    }

    @MethodMappedAs("setBlock")
    public void setBlockState(WrappedBlockState state) {
        setBlockCombinedId(ReflectionManager.getCombinedIdByWrappedBlockState(state));
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

        setBlockCombinedId(ReflectionManager.getCombinedIdByBlockData(data));
    }

    private void setBlockCombinedId(int combinedId) {
        this.blockCombinedId = combinedId;

        DisguiseParser.updateDisguiseName(getDisguise());

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

    @Override
    public double getWidthX() {
        return 1;
    }

    @Override
    public double getWidthZ() {
        return 1;
    }
}
