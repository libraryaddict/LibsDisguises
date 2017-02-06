package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.block.BlockFace;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.Direction;
import com.google.common.base.Optional;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * @author Navid
 */
public class ShulkerWatcher extends InsentientWatcher {

    public ShulkerWatcher(Disguise disguise) {
        super(disguise);
    }

    public BlockFace getFacingDirection() {
        return BlockFace.valueOf(getData(MetaIndex.SHULKER_FACING).name());
    }

    public void setFacingDirection(BlockFace face) {
        setData(MetaIndex.SHULKER_FACING, Direction.valueOf(face.name()));
        sendData(MetaIndex.SHULKER_FACING);
    }

    public BlockPosition getAttachmentPosition() {
        return getData(MetaIndex.SHULKER_ATTACHED).get();
    }

    public void setAttachmentPosition(BlockPosition pos) {
        setData(MetaIndex.SHULKER_ATTACHED, Optional.of(pos));
        sendData(MetaIndex.SHULKER_ATTACHED);
    }

    public int getShieldHeight() {
        return getData(MetaIndex.SHULKER_PEEKING);
    }

    public void setShieldHeight(int newHeight) {
        if (newHeight < 0)
            newHeight = 0;

        if (newHeight > 127)
            newHeight = 127;

        setData(MetaIndex.SHULKER_PEEKING, (byte) newHeight);
        sendData(MetaIndex.SHULKER_PEEKING);
    }

    public void setColor(AnimalColor color) {
        setData(MetaIndex.SHULKER_COLOR, (byte) color.getId());
        sendData(MetaIndex.SHULKER_COLOR);
    }
}
