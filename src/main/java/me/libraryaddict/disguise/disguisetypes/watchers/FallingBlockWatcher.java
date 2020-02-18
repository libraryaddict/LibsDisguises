package me.libraryaddict.disguise.disguisetypes.watchers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;

public class FallingBlockWatcher extends FlagWatcher {
    private ItemStack block = new ItemStack(Material.STONE);
    private boolean gridLocked;

    public FallingBlockWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    public FallingBlockWatcher clone(Disguise disguise) {
        FallingBlockWatcher watcher = (FallingBlockWatcher) super.clone(disguise);
        watcher.setBlock(getBlock().clone());

        return watcher;
    }

    public boolean isGridLocked() {
        return gridLocked;
    }

    public void setGridLocked(boolean gridLocked) {
        if (isGridLocked() == gridLocked) {
            return;
        }

        this.gridLocked = gridLocked;

        if (getDisguise().isDisguiseInUse() && getDisguise().getEntity() != null) {
            PacketContainer relMove = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE);
            StructureModifier<Short> shorts = relMove.getShorts();
            Location loc = getDisguise().getEntity().getLocation();

            relMove.getModifier().write(0, getDisguise().getEntity().getEntityId());
            shorts.write(0, conRel(loc.getX(), loc.getBlockX() + 0.5));
            shorts.write(1, conRel(loc.getY(), loc.getBlockY()));
            shorts.write(2, conRel(loc.getZ(), loc.getBlockZ() + 0.5));

            try {
                for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
                    if (player == getDisguise().getEntity()) {
                        PacketContainer temp = relMove.shallowClone();
                        temp.getModifier().write(0, DisguiseAPI.getSelfDisguiseId());

                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, temp, isGridLocked());
                    } else {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, relMove, isGridLocked());
                    }
                }
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private short conRel(double oldCord, double newCord) {
        return (short) (((oldCord - newCord) * 4096) * (isGridLocked() ? -1 : 1));
    }

    public ItemStack getBlock() {
        return block;
    }

    public void setBlock(ItemStack block) {
        if (block == null || block.getType() == null || block.getType() == Material.AIR || !block.getType().isBlock()) {
            block = new ItemStack(Material.STONE);
        }

        this.block = block;

        if (DisguiseAPI.isDisguiseInUse(getDisguise()) && getDisguise().getWatcher() == this) {
            DisguiseUtilities.refreshTrackers(getDisguise());
        }
    }
}
