package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

import java.awt.*;

/**
 * @author Navid
 */
public class AreaEffectCloudWatcher extends FlagWatcher {

    public AreaEffectCloudWatcher(Disguise disguise) {
        super(disguise);
    }

    public float getRadius() {
        return (float) getValue(5, 0f);
    }

    public int getColor() {
        return (int) getValue(6, Color.BLACK.getRGB());
    }

    public boolean isIgnoreRadius() {
        return (boolean) getValue(7, false);
    }

    public int getParticleId() {
        return (int) getValue(8, 0);
    }

    public void setRadius(float radius) {
        setValue(5, radius);
    }

    public void setColor(int color) {
        setValue(6, color);
    }

    public void setIgnoreRadius(boolean ignore) {
        setValue(7, ignore);
    }

    public void setParticleId(int particleId) {
        setValue(8, particleId);
    }


}
