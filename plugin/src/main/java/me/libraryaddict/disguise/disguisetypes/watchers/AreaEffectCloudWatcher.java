package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleType;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Color;
import org.bukkit.Particle;

/**
 * @author Navid
 */
public class AreaEffectCloudWatcher extends FlagWatcher {

    public AreaEffectCloudWatcher(Disguise disguise) {
        super(disguise);

        if (!NmsVersion.v1_20_R4.isSupported() && DisguiseConfig.isRandomDisguises()) {
            setColor(Color.fromRGB(RandomUtils.nextInt(256), RandomUtils.nextInt(256), RandomUtils.nextInt(256)));
        }
    }

    public float getRadius() {
        return getData(MetaIndex.AREA_EFFECT_RADIUS);
    }

    @MethodDescription("How big is this Area Effect Cloud?")
    public void setRadius(float radius) {
        if (radius > 30) {
            radius = 30;
        } else if (radius < 0.1) {
            radius = 0.1f;
        }

        sendData(MetaIndex.AREA_EFFECT_RADIUS, radius);
    }

    @NmsRemovedIn(NmsVersion.v1_20_R4)
    public Color getColor() {
        return getData(MetaIndex.AREA_EFFECT_CLOUD_COLOR);
    }

    @RandomDefaultValue
    @MethodDescription("What's the color of this Area Effect Cloud?")
    @NmsRemovedIn(NmsVersion.v1_20_R4)
    public void setColor(Color color) {
        sendData(MetaIndex.AREA_EFFECT_CLOUD_COLOR, color);
    }

    public boolean isIgnoreRadius() {
        return getData(MetaIndex.AREA_EFFECT_IGNORE_RADIUS);
    }

    @MethodDescription
    public void setIgnoreRadius(boolean ignore) {
        sendData(MetaIndex.AREA_EFFECT_IGNORE_RADIUS, ignore);
    }

    @NmsAddedIn(NmsVersion.v1_13)
    public com.github.retrooper.packetevents.protocol.particle.Particle<?> getParticle() {
        if (NmsVersion.v1_13.isSupported()) {
            return getData(MetaIndex.AREA_EFFECT_PARTICLE);
        } else {
            // Item crack, block crack, block dust, falling dust
            int particleId = getData(MetaIndex.AREA_EFFECT_PARTICLE_OLD);

            return new com.github.retrooper.packetevents.protocol.particle.Particle<ParticleData>(
                (ParticleType<ParticleData>) ParticleTypes.getById(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(),
                    particleId));
        }
    }

    @NmsAddedIn(NmsVersion.v1_13)
    @MethodDescription("What particle is this Area Effect Cloud using?")
    public void setParticle(com.github.retrooper.packetevents.protocol.particle.Particle particle) {
        if (NmsVersion.v1_13.isSupported()) {
            sendData(MetaIndex.AREA_EFFECT_PARTICLE, particle);
        } else {
            setParticleType((Particle) SpigotConversionUtil.toBukkitParticle(particle.getType()));
        }
    }

    @Deprecated
    public Particle getParticleType() {
        if (NmsVersion.v1_13.isSupported()) {
            return (Particle) SpigotConversionUtil.toBukkitParticle(getData(MetaIndex.AREA_EFFECT_PARTICLE).getType());
        } else {
            return ReflectionManager.fromEnum(Particle.class, getData(MetaIndex.AREA_EFFECT_PARTICLE_OLD));
        }
    }

    @Deprecated
    @MethodDescription("What particle type is this Area Effect Cloud using?")
    public void setParticleType(Particle particle) {
        if (NmsVersion.v1_13.isSupported()) {
            setParticle(
                new com.github.retrooper.packetevents.protocol.particle.Particle(SpigotConversionUtil.fromBukkitParticle(particle)));
        } else {
            sendData(MetaIndex.AREA_EFFECT_PARTICLE_OLD, ReflectionManager.enumOrdinal(particle));
        }
    }
}
