package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedEntity;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedPlayer;
import org.bukkit.DyeColor;
import org.bukkit.Location;

public class CushionWatcher extends FlagWatcher implements GridLockedWatcher {
    @Getter
    private boolean gridLocked;

    public CushionWatcher(Disguise disguise) {
        super(disguise);
    }

    public DyeColor getColor() {
        return AnimalColor.valueOf(getData(MetaIndex.CUSHION_COLOR).name()).getDyeColor();
    }

    public void setColor(DyeColor newColor) {
        sendData(MetaIndex.CUSHION_COLOR, com.github.retrooper.packetevents.protocol.color.DyeColor.valueOf(
            newColor == null ? "WHITE" : AnimalColor.getColor(newColor).name()));
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

        IWrappedEntity entity = getDisguise().getWrappedEntity();

        for (IWrappedPlayer player : DisguiseUtilities.getTrackingPlayers(getDisguise())) {
            int entityId = entity == player ? DisguiseAPI.getSelfDisguiseId() : entity.getEntityId();

            WrapperPlayServerEntityRelativeMove relMov = new WrapperPlayServerEntityRelativeMove(entityId, x, y, z, true);

            if (isGridLocked()) {
                player.sendPacket(relMov);
            } else {
                player.sendPacketSilently(relMov);
            }
        }
    }

    private short conRel(double oldCord, double newCord) {
        return (short) (((oldCord - newCord) * 4096) * (isGridLocked() ? -1 : 1));
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
