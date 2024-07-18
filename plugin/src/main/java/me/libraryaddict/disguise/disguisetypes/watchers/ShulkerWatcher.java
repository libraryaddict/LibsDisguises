package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.util.Vector3i;
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
        sendData(MetaIndex.SHULKER_FACING, com.github.retrooper.packetevents.protocol.world.BlockFace.valueOf(face.name()));
    }

    public Vector3i getAttachmentPosition() {
        return getData(MetaIndex.SHULKER_ATTACHED).orElse(Vector3i.zero());
    }

    public void setAttachmentPosition(Vector3i pos) {
        sendData(MetaIndex.SHULKER_ATTACHED, Optional.ofNullable(pos));
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

        sendData(MetaIndex.SHULKER_PEEKING, (byte) newHeight);
    }

    public DyeColor getColor() {
        if (!hasValue(MetaIndex.SHULKER_COLOR) || getData(MetaIndex.SHULKER_COLOR) == (byte) 16) {
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

        sendData(MetaIndex.SHULKER_COLOR, newColor == null ? (byte) 16 : newColor.getWoolData());
    }
}
