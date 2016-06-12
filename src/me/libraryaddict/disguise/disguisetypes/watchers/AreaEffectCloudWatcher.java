package me.libraryaddict.disguise.disguisetypes.watchers;

import java.awt.Color;

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
        return getValue(FlagType.AREA_EFFECT_RADIUS);
    }

    public int getColor()
    {
        return getValue(FlagType.AREA_EFFECT_COLOR);
    }

    public boolean isIgnoreRadius()
    {
        return getValue(FlagType.AREA_EFFECT_IGNORE_RADIUS);
    }

    public int getParticleId()
    {
        return getValue(FlagType.AREA_EFFECT_PARTICLE);
    }

    public void setRadius(float radius)
    {
        setValue(FlagType.AREA_EFFECT_RADIUS, radius);
    }

    public void setColor(int color)
    {
        setValue(FlagType.AREA_EFFECT_COLOR, color);
    }

    public void setIgnoreRadius(boolean ignore)
    {
        setValue(FlagType.AREA_EFFECT_IGNORE_RADIUS, ignore);
    }

    public void setParticleId(int particleId)
    {
        setValue(FlagType.AREA_EFFECT_PARTICLE, particleId);
    }

}
