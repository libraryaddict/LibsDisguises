package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;

public class CamelWatcher extends AbstractHorseWatcher {
    public CamelWatcher(Disguise disguise) {
        super(disguise);
    }

    private int getPoseTransitionTime(EntityPose pose) {
        switch (pose) {
            case SITTING:
                return 40;
            case STANDING:
                return 52;
            default:
                return 0;
        }
    }

    public boolean isDashing() {
        return getData(MetaIndex.CAMEL_DASHING);
    }

    @MethodDescription("Is this Camel dashing?")
    public void setDashing(boolean dashing) {
        sendData(MetaIndex.CAMEL_DASHING, dashing);
    }

    // Technically added in v1_19_R2, but API is in v1_19_R3
    @NmsAddedIn(NmsVersion.v1_19_R3)
    public void setSitting(boolean sitting) {
        setEntityPose(sitting ? EntityPose.SITTING : EntityPose.STANDING);
    }

    @NmsAddedIn(NmsVersion.v1_19_R3)
    public boolean isSitting() {
        return getEntityPose() == EntityPose.SITTING;
    }

    @Override
    @NmsAddedIn(NmsVersion.v1_14)
    public void setEntityPose(EntityPose entityPose) {
        EntityPose oldPose = getEntityPose();

        super.setEntityPose(entityPose);

        if (!NmsVersion.v1_19_R3.isSupported() || oldPose == entityPose) {
            return;
        }

        long time;

        if (getDisguise() != null && getDisguise().isDisguiseInUse() && getDisguise().getEntity() != null) {
            time = ReflectionManager.getGameTime(getDisguise().getEntity());

            if (entityPose == EntityPose.SITTING) {
                time = -time;
            }
        } else {
            time = entityPose == EntityPose.SITTING ? 1_000_000_000L : 0;
        }

        sendData(MetaIndex.CAMEL_LAST_POSE_CHANGED, time);
    }

    @Override
    @NmsAddedIn(NmsVersion.v1_14)
    public EntityPose getEntityPose() {
        return super.getEntityPose();
    }
}
