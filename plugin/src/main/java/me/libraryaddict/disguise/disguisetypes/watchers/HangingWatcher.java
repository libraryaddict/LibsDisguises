package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;

public class HangingWatcher extends FlagWatcher {
    public HangingWatcher(Disguise disguise) {
        super(disguise);
    }

    @NmsAddedIn(NmsVersion.v1_21_R5)
    @Deprecated
    public org.bukkit.block.BlockFace getFacing() {
        return org.bukkit.block.BlockFace.valueOf(getData(MetaIndex.HANGING_DIRECTION).name());
    }

    @NmsAddedIn(NmsVersion.v1_21_R5)
    @Deprecated
    public void setFacing(org.bukkit.block.BlockFace face) {
        sendData(MetaIndex.HANGING_DIRECTION, com.github.retrooper.packetevents.protocol.world.BlockFace.valueOf(face.name()));
    }
}
