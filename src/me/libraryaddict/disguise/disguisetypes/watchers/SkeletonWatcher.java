package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.entity.Skeleton.SkeletonType;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

/**
 * @author Navid
 */
public class SkeletonWatcher extends InsentientWatcher
{
    public SkeletonWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public void setSwingArms(boolean swingingArms)
    {
        setData(FlagType.SKELETON_SWING_ARMS, swingingArms);
        sendData(FlagType.SKELETON_SWING_ARMS);
    }

    public boolean isSwingArms()
    {
        return getData(FlagType.SKELETON_SWING_ARMS);
    }

    public void setType(SkeletonType type)
    {
        setData(FlagType.SKELETON_TYPE, type.ordinal());
        sendData(FlagType.SKELETON_TYPE);
    }

    public SkeletonType getType()
    {
        return SkeletonType.values()[getData(FlagType.SKELETON_TYPE)];
    }
}
