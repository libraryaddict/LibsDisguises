package me.libraryaddict.disguise.disguisetypes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

@RequiredArgsConstructor
/**
 * Not intended to be used by external plugins
 */ public class DisguiseInternals<D extends Disguise> {
    private final D disguise;
    /**
     * The entity scale when Libs Disguises is not excluded from attributes
     */
    private double entityScaleWithLibsDisguises = Double.MIN_VALUE;
    /**
     * The entity scale when Libs Disguises is excluded from attributes
     */
    private double entityScaleWithoutLibsDisguises = Double.MIN_VALUE;
    /**
     * The entity scale that was last sent in a packet, used to try avoid wasting our time
     */
    private double entityScaleLastSentViaPackets = Double.MIN_VALUE;
    /**
     * The biggest we'll allow the self disguise to be scaled up to, including disguise applied scale
     */
    @Setter
    @Getter
    private double selfDisguiseTallScaleMax = 1;
    @Getter
    private final NamespacedKey bossBar = new NamespacedKey("libsdisguises", UUID.randomUUID().toString());

    protected double getActualEntityScale() {
        return ((LivingEntity) disguise.getEntity()).getAttribute(DisguiseUtilities.getScaleAttribute()).getValue();
    }

    protected double getRawEntityScaleWithoutLibsDisguises() {
        return DisguiseUtilities.getEntityScaleWithoutLibsDisguises(disguise.getEntity());
    }

    public double getEntityScaleWithoutLibsDisguises() {
        double actualScale = getActualEntityScale();

        // If nothing has changed
        if (entityScaleWithLibsDisguises != actualScale) {
            // Update the packet field as well, as we expect it to change
            entityScaleWithLibsDisguises = entityScaleLastSentViaPackets = actualScale;
            entityScaleWithoutLibsDisguises = getRawEntityScaleWithoutLibsDisguises();
        }

        return entityScaleWithoutLibsDisguises;
    }

    public double getPacketEntityScale(double scaleInPacket) {
        if (entityScaleLastSentViaPackets == scaleInPacket) {
            return entityScaleWithoutLibsDisguises;
        }

        entityScaleLastSentViaPackets = scaleInPacket;

        // If disguise cant be scaled
        // If a scale cannot be applied to the entity (or it is null)
        if (disguise.isMiscDisguise() || !(disguise.getEntity() instanceof LivingEntity)) {
            return scaleInPacket;
        }

        getEntityScaleWithoutLibsDisguises();
        entityScaleLastSentViaPackets = scaleInPacket;

        return entityScaleWithoutLibsDisguises;
    }
}
