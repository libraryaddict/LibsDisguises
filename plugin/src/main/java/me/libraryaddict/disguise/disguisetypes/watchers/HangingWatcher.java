package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

public class HangingWatcher extends FlagWatcher implements GridLockedWatcher{
    private volatile int lastRotation = -1;

    public HangingWatcher(Disguise disguise) {
        super(disguise);
    }

    public void updateHangingRotation() {
        if (!NmsVersion.v1_21_R5.isSupported()) {
            return;
        }

        Entity entity;

        // If disguise is null or entity is null
        if (getDisguise() == null || (entity = getDisguise().getEntity()) == null) {
            return;
        }

        Float yaw = getYawLock();

        // Use yaw lock, otherwise use entity yaw
        if (yaw == null) {
            yaw = DisguiseUtilities.getYaw(DisguiseType.getType(entity.getType()), entity.getLocation().getYaw());
        }

        // Fix yaw for the disguise type
        yaw = DisguiseUtilities.getYaw(getDisguise().getType(), yaw);

        // Convert yaw to an int from 0 to 3
        int ordinal = DisguiseUtilities.getHangingOrdinal(yaw);

        // If ordinal matches last set rotation
        if (ordinal == lastRotation) {
            return;
        }

        // Set the last rotation
        lastRotation = ordinal;

        // Get the black face
        BlockFace face = BlockFace.values()[ordinal];
        // Set the metadata, resolve it via name
        sendData(MetaIndex.HANGING_DIRECTION, com.github.retrooper.packetevents.protocol.world.BlockFace.valueOf(face.name()));
    }

    @Override
    @Deprecated
    public double getWidthX() {
        return 1;
    }

    @Override
    @Deprecated
    public double getWidthZ() {
        return 1;
    }

    @Override
    @Deprecated
    public boolean isGridLocked() {
        return true;
    }

    @Override
    @Deprecated
    public void setGridLocked(boolean gridLocked) {
    }
}
