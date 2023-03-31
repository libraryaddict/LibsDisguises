package me.libraryaddict.disguise.disguisetypes.watchers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FallingBlockWatcher extends FlagWatcher {
    private int blockCombinedId = 1;
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
            relMove.getModifier().write(0, getDisguise().getEntity().getEntityId());

            Location loc = getDisguise().getEntity().getLocation();

            if (NmsVersion.v1_14.isSupported()) {
                StructureModifier<Short> shorts = relMove.getShorts();

                shorts.write(0, conRel(loc.getX(), loc.getBlockX() + 0.5));
                shorts.write(1, conRel(loc.getY(), loc.getBlockY() + (loc.getY() % 1 >= 0.85 ? 1 : loc.getY() % 1 >= 0.35 ? .5 : 0)));
                shorts.write(2, conRel(loc.getZ(), loc.getBlockZ() + 0.5));
            } else {
                StructureModifier<Integer> ints = relMove.getIntegers();

                ints.write(0, (int) conRel(loc.getX(), loc.getBlockX() + 0.5));
                ints.write(1, (int) conRel(loc.getY(), loc.getBlockY() + (loc.getY() % 1 >= 0.85 ? 1 : loc.getY() % 1 >= 0.35 ? .5 : 0)));
                ints.write(2, (int) conRel(loc.getZ(), loc.getBlockZ() + 0.5));
            }

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
    }

    private short conRel(double oldCord, double newCord) {
        return (short) (((oldCord - newCord) * 4096) * (isGridLocked() ? -1 : 1));
    }

    public ItemStack getBlock() {
        return ReflectionManager.getItemStackByCombinedId(getBlockCombinedId());
    }

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

        this.blockCombinedId = ReflectionManager.getCombinedIdByBlockData(data);

        if (!getDisguise().isCustomDisguiseName()) {
            getDisguise().setDisguiseName(TranslateType.DISGUISE_OPTIONS_PARAMETERS.get("Block") + " " +
                TranslateType.DISGUISE_OPTIONS_PARAMETERS.get(ReflectionManager.toReadable(data.getMaterial().name(), " ")));
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
