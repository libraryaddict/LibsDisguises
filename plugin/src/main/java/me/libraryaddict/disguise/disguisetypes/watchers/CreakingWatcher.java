package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;

public class CreakingWatcher extends InsentientWatcher {
    public CreakingWatcher(Disguise disguise) {
        super(disguise);
    }

    @MethodDescription("If set to false, Creaking will not play 'movement' animations")
    public void setMovementAnimations(boolean canMove) {
        sendData(MetaIndex.CREAKING_CAN_MOVE, canMove);
    }

    public boolean isMovementAnimations() {
        return getData(MetaIndex.CREAKING_CAN_MOVE);
    }

    public void setBrightEyes(boolean active) {
        sendData(MetaIndex.CREAKING_IS_ACTIVE, active);
    }

    public boolean isBrightEyes() {
        return getData(MetaIndex.CREAKING_IS_ACTIVE);
    }

    @MethodDescription("If set to true, Creaking will play a 'destruction' animation that does not loop")
    public void setTearingDown(boolean isTearingDown) {
        sendData(MetaIndex.CREAKING_IS_TEARING_DOWN, isTearingDown);
    }

    public boolean isTearingDown() {
        return getData(MetaIndex.CREAKING_IS_TEARING_DOWN);
    }
}
