package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.block.BlockFace;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.Direction;
import com.google.common.base.Optional;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

/**
 * @author Navid
 */
public class ShulkerWatcher extends InsentientWatcher
{

    public ShulkerWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public BlockFace getFacingDirection()
    {
        return BlockFace.valueOf(getValue(FlagType.SHULKER_FACING).name());
    }

    public void setFacingDirection(BlockFace face)
    {
        setValue(FlagType.SHULKER_FACING, Direction.valueOf(face.name()));
        sendData(FlagType.SHULKER_FACING);
    }

    public BlockPosition getAttachmentPosition()
    {
        return getValue(FlagType.SHULKER_ATTACHED).get();
    }

    public void setAttachmentPosition(BlockPosition pos)
    {
        setValue(FlagType.SHULKER_ATTACHED, Optional.of(pos));
        sendData(FlagType.SHULKER_ATTACHED);
    }

    public int getShieldHeight()
    {
        return getValue(FlagType.SHULKER_PEEKING);
    }

    public void setShieldHeight(int newHeight)
    {
        if (newHeight < 0)
            newHeight = 0;

        if (newHeight > 127)
            newHeight = 127;

        setValue(FlagType.SHULKER_PEEKING, (byte) newHeight);
        sendData(FlagType.SHULKER_PEEKING);
    }

}
