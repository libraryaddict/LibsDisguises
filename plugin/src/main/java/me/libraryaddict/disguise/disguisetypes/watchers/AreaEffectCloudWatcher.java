package me.libraryaddict.disguise.disguisetypes.watchers;

import com.comphenix.protocol.wrappers.WrappedParticle;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Color;
import org.bukkit.Particle;

/**
 * @author Navid
 */
public class AreaEffectCloudWatcher extends FlagWatcher {

    public AreaEffectCloudWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            setColor(Color.fromRGB(RandomUtils.nextInt(256), RandomUtils.nextInt(256), RandomUtils.nextInt(256)));
        }
    }

    public float getRadius() {
        return getData(MetaIndex.AREA_EFFECT_RADIUS);
    }

    public void setRadius(float radius) {
        if (radius > 30) {
            radius = 30;
        } else if (radius < 0.1) {
            radius = 0.1f;
        }

        setData(MetaIndex.AREA_EFFECT_RADIUS, radius);
        sendData(MetaIndex.AREA_EFFECT_RADIUS);
    }

    public Color getColor() {
        int color = getData(MetaIndex.AREA_EFFECT_CLOUD_COLOR);
        return Color.fromRGB(color);
    }

    @RandomDefaultValue
    public void setColor(Color color) {
        setData(MetaIndex.AREA_EFFECT_CLOUD_COLOR, color.asRGB());
        sendData(MetaIndex.AREA_EFFECT_CLOUD_COLOR);
    }

    public boolean isIgnoreRadius() {
        return getData(MetaIndex.AREA_EFFECT_IGNORE_RADIUS);
    }

    public void setIgnoreRadius(boolean ignore) {
        setData(MetaIndex.AREA_EFFECT_IGNORE_RADIUS, ignore);
        sendData(MetaIndex.AREA_EFFECT_IGNORE_RADIUS);
    }

    @NmsAddedIn(NmsVersion.v1_13)
    public <T> void setParticle(Particle particle, T particleData) {
        setParticle(WrappedParticle.create(particle, particleData));
    }

    @NmsAddedIn(NmsVersion.v1_13)
    public WrappedParticle getParticle() {
        if (NmsVersion.v1_13.isSupported()) {
            return getData(MetaIndex.AREA_EFFECT_PARTICLE);
        } else {
            // Item crack, block crack, block dust, falling dust
            int particleId = getData(MetaIndex.AREA_EFFECT_PARTICLE_OLD);
            Particle particle = Particle.values()[particleId];

            return WrappedParticle.create(particle, null);
        }
    }

    @NmsAddedIn(NmsVersion.v1_13)
    public void setParticle(WrappedParticle particle) {
        if (NmsVersion.v1_13.isSupported()) {
            setData(MetaIndex.AREA_EFFECT_PARTICLE, particle);
            sendData(MetaIndex.AREA_EFFECT_PARTICLE);
        } else {
            setParticleType(particle.getParticle());
        }
    }

    public Particle getParticleType() {
        if (NmsVersion.v1_13.isSupported()) {
            return getParticle().getParticle();
        } else {
            return Particle.values()[getData(MetaIndex.AREA_EFFECT_PARTICLE_OLD)];
        }
    }

    public void setParticleType(Particle particle) {
        if (NmsVersion.v1_13.isSupported()) {
            setParticle(WrappedParticle.create(particle, null));
        } else {
            setData(MetaIndex.AREA_EFFECT_PARTICLE_OLD, particle.ordinal());

            sendData(MetaIndex.AREA_EFFECT_PARTICLE_OLD);
        }
    }
}
