package me.libraryaddict.disguise.disguisetypes.watchers;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.Direction;
import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.DyeColor;
import org.bukkit.block.BlockFace;

import java.util.Optional;

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
        return getData(MetaIndex.SHULKER_ATTACHED).orElse(BlockPosition.ORIGIN);
    }

    public void setAttachmentPosition(BlockPosition pos) {
        setData(MetaIndex.SHULKER_ATTACHED, Optional.of(pos));
        sendData(MetaIndex.SHULKER_ATTACHED);
    }

    public int getShieldHeight() {
        return getData(MetaIndex.SHULKER_PEEKING);
    }

    public void setShieldHeight(int newHeight) {
        if (newHeight < 0) {
            newHeight = 0;
        }

        if (newHeight > 127) {
            newHeight = 127;
        }

        setData(MetaIndex.SHULKER_PEEKING, (byte) newHeight);
        sendData(MetaIndex.SHULKER_PEEKING);
    }

    public DyeColor getColor() {
        if (!hasValue(MetaIndex.SHULKER_COLOR)) {
            return DyeColor.PURPLE;
        }

        return AnimalColor.getColorByWool(getData(MetaIndex.SHULKER_COLOR)).getDyeColor();
    }

    @Deprecated
    public void setColor(AnimalColor color) {
        setColor(color.getDyeColor());
    }

    public void setColor(DyeColor newColor) {
        if (newColor == getColor()) {
            return;
        }

        setData(MetaIndex.SHULKER_COLOR, newColor.getWoolData());
        sendData(MetaIndex.SHULKER_COLOR);
    }
}
