package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.block.BlockFace;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.google.common.base.Optional;

import me.libraryaddict.disguise.disguisetypes.Disguise;

/**
 * @author Navid
 */
// TODO: Add the appropriate data values to this class
public class ShulkerWatcher extends LivingWatcher
{

    public ShulkerWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public BlockFace getFacingDirection()
    {
        return BlockFace.UP;
    }

    public void setFacingDirection()
    {

    }

    public Optional<BlockPosition> getAttachmentPosition()
    {
        return Optional.absent();
    }

    public void setAttachmentPosition(BlockPosition pos)
    {

    }

    public byte getShieldHeight()
    {
        return 0x00;
    }

    public void setShieldHeight()
    {

    }

}
