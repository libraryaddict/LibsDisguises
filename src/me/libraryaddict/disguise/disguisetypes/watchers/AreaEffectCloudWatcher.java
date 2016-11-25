package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

/**
 * @author Navid
 */
public class AreaEffectCloudWatcher extends FlagWatcher
{

    public AreaEffectCloudWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public float getRadius()
    {
        return getData(FlagType.AREA_EFFECT_RADIUS);
    }

    public int getColor()
    {
        return getData(FlagType.AREA_EFFECT_COLOR);
    }

    public boolean isIgnoreRadius()
    {
        return getData(FlagType.AREA_EFFECT_IGNORE_RADIUS);
    }

    public int getParticleId()
    {
        return getData(FlagType.AREA_EFFECT_PARTICLE);
    }

    public void setRadius(float radius)
    {
        setData(FlagType.AREA_EFFECT_RADIUS, radius);
    }

    public void setColor(int color)
    {
        setData(FlagType.AREA_EFFECT_COLOR, color);
    }

    public void setIgnoreRadius(boolean ignore)
    {
        setData(FlagType.AREA_EFFECT_IGNORE_RADIUS, ignore);
    }

    public void setParticleId(int particleId)
    {
        setData(FlagType.AREA_EFFECT_PARTICLE, particleId);
    }

}
