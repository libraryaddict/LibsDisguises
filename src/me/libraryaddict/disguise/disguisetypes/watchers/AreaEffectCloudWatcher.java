package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

/**
 * @author Navid
 */
public class AreaEffectCloudWatcher extends FlagWatcher {

    public AreaEffectCloudWatcher(Disguise disguise) {
        super(disguise);

        setRadius(1);
    }

    public float getRadius() {
        return getData(MetaIndex.AREA_EFFECT_RADIUS);
    }

    public int getColor() {
        return getData(MetaIndex.AREA_EFFECT_CLOUD_COLOR);
    }

    public boolean isIgnoreRadius() {
        return getData(MetaIndex.AREA_EFFECT_IGNORE_RADIUS);
    }

    public int getParticleId() {
        return getData(MetaIndex.AREA_EFFECT_PARTICLE);
    }

    public void setRadius(float radius) {
        if (radius > 30)
            radius = 30;

        setData(MetaIndex.AREA_EFFECT_RADIUS, radius);
        sendData(MetaIndex.AREA_EFFECT_RADIUS);
    }

    public void setColor(int color) {
        setData(MetaIndex.AREA_EFFECT_CLOUD_COLOR, color);
        sendData(MetaIndex.AREA_EFFECT_CLOUD_COLOR);
    }

    public void setIgnoreRadius(boolean ignore) {
        setData(MetaIndex.AREA_EFFECT_IGNORE_RADIUS, ignore);
        sendData(MetaIndex.AREA_EFFECT_IGNORE_RADIUS);
    }

    public void setParticleId(int particleId) {
        setData(MetaIndex.AREA_EFFECT_PARTICLE, particleId);
        sendData(MetaIndex.AREA_EFFECT_PARTICLE);
    }
}
