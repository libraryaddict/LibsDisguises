package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodMappedAs;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.joml.Vector3f;

public class BlockDisplayWatcher extends DisplayWatcher implements GridLockedWatcher {
    private boolean autoCenter = true;
    @Getter
    private boolean gridLocked;

    public BlockDisplayWatcher(Disguise disguise) {
        super(disguise);

        // So we're not seeing air
        setBlockState(WrappedBlockState.getDefaultState(StateTypes.STONE));
        // So its centered
        adjustCenter();
    }

    @Override
    public BlockDisplayWatcher clone(Disguise owningDisguise) {
        BlockDisplayWatcher displayWatcher = (BlockDisplayWatcher) super.clone(owningDisguise);
        displayWatcher.setAutoCentered(autoCenter);

        return displayWatcher;
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

    public boolean isAutoCentered() {
        return autoCenter;
    }

    public void setAutoCentered(boolean autoCenter) {
        this.autoCenter = autoCenter;

        adjustCenter();
    }

    @Override
    // Because BlockDisplayWatcher modifies this on startup..
    @RandomDefaultValue
    public void setTranslation(Vector3f translation) {
        super.setTranslation(translation);

        // Detect if this is centered or not, if its not centered, we don't want to ruin their settings
        Vector3f scale = getScale();

        // We always set a 0 y
        // We always set the X and Z to be half of the scale, negative
        if (translation.y() == 0 && translation.x() == -scale.x() / 2f && translation.z() == -scale.z() / 2f) {
            return;
        }

        setAutoCentered(false);
    }

    @Override
    public Vector3f getTranslation() {
        return super.getTranslation();
    }

    private void adjustCenter() {
        if (!isAutoCentered()) {
            return;
        }

        Vector3f scale = getScale();
        Vector3f oldTranslation = getTranslation();
        Vector3f newTranslation = new Vector3f(-scale.x() / 2f, 0f, -scale.z() / 2f);

        if (oldTranslation.x() == newTranslation.x() && oldTranslation.y() == newTranslation.y() &&
            oldTranslation.z() == newTranslation.z()) {
            return;
        }

        setTranslation(newTranslation);
    }

    @Override
    public Vector3f getScale() {
        return super.getScale();
    }

    @Override
    public void setScale(Vector3f scale) {
        super.setScale(scale);

        adjustCenter();
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

    @Override
    public double getWidthX() {
        return getScale().x;
    }

    @Override
    public double getWidthZ() {
        return getScale().z;
    }
}
