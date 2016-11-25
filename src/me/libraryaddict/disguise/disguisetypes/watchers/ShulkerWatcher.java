package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.block.BlockFace;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.Direction;
import com.google.common.base.Optional;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

/**
 * @author Navid
 */
public class ShulkerWatcher extends InsentientWatcher {

    public ShulkerWatcher(Disguise disguise) {
        super(disguise);
    }

    public BlockFace getFacingDirection() {
        return BlockFace.valueOf(getData(FlagType.SHULKER_FACING).name());
    }

    public void setFacingDirection(BlockFace face) {
        setData(FlagType.SHULKER_FACING, Direction.valueOf(face.name()));
        sendData(FlagType.SHULKER_FACING);
    }

    public BlockPosition getAttachmentPosition() {
        return getData(FlagType.SHULKER_ATTACHED).get();
    }

    public void setAttachmentPosition(BlockPosition pos) {
        setData(FlagType.SHULKER_ATTACHED, Optional.of(pos));
        sendData(FlagType.SHULKER_ATTACHED);
    }

    public int getShieldHeight() {
        return getData(FlagType.SHULKER_PEEKING);
    }

    public void setShieldHeight(int newHeight) {
        if (newHeight < 0)
            newHeight = 0;

        if (newHeight > 127)
            newHeight = 127;

        setData(FlagType.SHULKER_PEEKING, (byte) newHeight);
        sendData(FlagType.SHULKER_PEEKING);
    }

    public void setColor(AnimalColor color) {
        setData(FlagType.SHULKER_COLOR, (byte) color.getId());
        sendData(FlagType.SHULKER_COLOR);
    }
}
