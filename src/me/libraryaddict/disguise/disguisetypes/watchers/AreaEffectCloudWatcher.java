package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.Particle;

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

    public void setParticleType(Particle particle) {
        setData(MetaIndex.AREA_EFFECT_PARTICLE, particle);
        sendData(MetaIndex.AREA_EFFECT_PARTICLE);
    }

    public Particle getParticleType() {
        return getData(MetaIndex.AREA_EFFECT_PARTICLE);
    }
}
