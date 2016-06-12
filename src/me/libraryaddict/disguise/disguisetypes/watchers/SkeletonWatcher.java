package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.entity.Skeleton.SkeletonType;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

/**
 * @author Navid
 */
public class SkeletonWatcher extends LivingWatcher
{
    public SkeletonWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public void setType(SkeletonType type)
    {
        setValue(FlagType.SKELETON_TYPE, type.ordinal());
        sendData(FlagType.SKELETON_TYPE);
    }

    public SkeletonType getType()
    {
        return SkeletonType.values()[getValue(FlagType.SKELETON_TYPE)];
    }
}
