package me.libraryaddict.disguise.disguisetypes;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.movements.InteractiveBoundingBox;
import me.libraryaddict.disguise.utilities.movements.MovementTracker;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.scaling.DisguiseScaling;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Not intended to be used by external plugins
 */
@RequiredArgsConstructor
public class DisguiseInternals<D extends Disguise> implements DisguiseScaling.DisguiseScalingInternals {
    @Getter(AccessLevel.PRIVATE)
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
    @Getter
    private final DisguiseScaling scaling;
    private final AtomicBoolean refreshingScaling = new AtomicBoolean(false);
    @Getter
    private transient DisguiseRunnable runnable;
    private final Set<UUID> seesDisguise = new HashSet<>();
    @Getter
    private final List<MovementTracker> trackers = new ArrayList<>();
    @Getter
    private InteractiveBoundingBox interactiveBoundingBox;

    public DisguiseInternals(D disguise) {
        this.disguise = disguise;
        scaling = new DisguiseScaling(this);
    }

    public void setInteractiveBoundingBox(InteractiveBoundingBox boundingBox) {
        if (getInteractiveBoundingBox() != null) {
            trackers.remove(getInteractiveBoundingBox());

            if (getDisguise().isDisguiseInUse()) {
                getInteractiveBoundingBox().onDespawn();
            }
        }

        interactiveBoundingBox = boundingBox;

        if (boundingBox == null) {
            return;
        }

        boundingBox.setDisguise(getDisguise());
        trackers.add(boundingBox);

        if (!getDisguise().isDisguiseInUse()) {
            return;
        }

        boundingBox.onSpawn();
    }

    /**
     * If the respective player has been sent the Spawn packets
     *
     * @param player
     * @return
     */
    public synchronized boolean shouldAvoidSendingPackets(Player player) {
        return !seesDisguise.contains(player.getUniqueId());
    }

    public synchronized void addSeen(Player player, boolean isSpawnElseRemove) {
        if (isSpawnElseRemove) {
            seesDisguise.add(player.getUniqueId());
        } else {
            seesDisguise.remove(player.getUniqueId());
        }
    }

    protected void onDisguiseStart() {
        if (runnable != null && !runnable.isCancelled()) {
            runnable.cancel();
        }

        // Clear the seen
        seesDisguise.clear();

        // A scheduler to clean up any unused disguises.
        runnable = new DisguiseRunnable(getDisguise());

        runnable.runTaskTimer(LibsDisguises.getInstance(), 1, 1);

        for (MovementTracker tracker : trackers) {
            for (int entityId : tracker.getOwnedEntityIds()) {
                DisguiseUtilities.getRemappedEntityIds().put(entityId, getDisguise().getEntity().getEntityId());
            }
        }
    }

    protected void onDisguiseStop() {
        for (MovementTracker tracker : trackers) {
            tracker.onDespawn();

            for (int entityId : tracker.getOwnedEntityIds()) {
                DisguiseUtilities.getRemappedEntityIds().remove(entityId);
            }
        }

        // Clear the seen
        seesDisguise.clear();

        if (runnable == null) {
            return;
        }

        runnable.cancel();
        runnable = null;
    }

    private void refreshScale() {
        // If value was already true, return. Otherwise, set to true
        if (refreshingScaling.getAndSet(true)) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!disguise.isDisguiseInUse()) {
                    return;
                }

                getScaling().adjustScaling();
                refreshingScaling.set(false);
            }
        }.runTask(LibsDisguises.getInstance());
    }

    protected double getActualEntityScale() {
        return disguise.getEntity() instanceof LivingEntity ?
            ((LivingEntity) disguise.getEntity()).getAttribute(DisguiseUtilities.getScaleAttribute()).getValue() : 1;
    }

    protected double getRawEntityScaleWithoutLibsDisguises() {
        return DisguiseUtilities.getEntityScaleWithoutLibsDisguises(disguise.getEntity());
    }

    @Override
    public double getPlayerScaleWithoutLibsDisguises() {
        double actualScale = getActualEntityScale();

        // If nothing has changed
        if (entityScaleWithLibsDisguises != actualScale) {
            // Update the packet field as well, as we expect it to change
            entityScaleWithLibsDisguises = entityScaleLastSentViaPackets = actualScale;
            entityScaleWithoutLibsDisguises = getRawEntityScaleWithoutLibsDisguises();
        }

        return entityScaleWithoutLibsDisguises;
    }

    @Override
    public double getPrevSelfDisguiseTallScaleMax() {
        return getSelfDisguiseTallScaleMax();
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

        refreshScale();

        return entityScaleWithoutLibsDisguises;
    }

    @Override
    public void sendTinyFigureScale(double tinyFigureScale) {
        // The scale of the self disguise, not the player
        WrapperPlayServerUpdateAttributes.Property property =
            new WrapperPlayServerUpdateAttributes.Property(Attributes.GENERIC_SCALE, tinyFigureScale, new ArrayList<>());

        WrapperPlayServerUpdateAttributes packet =
            new WrapperPlayServerUpdateAttributes(DisguiseAPI.getSelfDisguiseId(), Collections.singletonList(property));

        PacketEvents.getAPI().getPlayerManager().sendPacket(getDisguise().getEntity(), packet);
    }

    @Override
    public void setPlayerScale(double personalPlayerScaleAttribute) {
        // Now we figure out the scale we need to have the player at the same eye level of the disguise
        AttributeInstance attribute = ((Player) getDisguise().getEntity()).getAttribute(DisguiseUtilities.getScaleAttribute());
        AttributeModifier modifier =
            attribute.getModifiers().stream().filter(a -> a.getKey().equals(DisguiseUtilities.getSelfDisguiseScaleNamespace())).findAny()
                .orElse(null);

        // Disabled or not allowed or doesn't need to scale
        if (!isScalePlayerToDisguise() || personalPlayerScaleAttribute == 1 || getDisguise().isPlayerDisguise()) {
            if (modifier != null) {
                attribute.removeModifier(modifier);
            }

            return;
        }

        // If the player does not get scaled to the disguise's viewpoint
        if (!isScalePlayerToDisguise()) {
            return;
        }

        // Subtract 1, as 1 is added internally
        personalPlayerScaleAttribute -= 1;

        if (modifier != null) {
            // Nothing changed, don't change anything
            if (modifier.getAmount() == personalPlayerScaleAttribute &&
                modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_SCALAR_1) {
                return;
            }

            attribute.removeModifier(modifier);
        }

        AttributeModifier newModifier = getAttributeModifier(personalPlayerScaleAttribute);

        attribute.addModifier(newModifier);
    }

    /**
     * If the disguise is in use, the disguised entity is a player and the disguised entity can see this disgiuse
     */
    @Override
    public boolean isScalingRelevant() {
        return getDisguise().isDisguiseInUse() && getDisguise().getEntity() instanceof Player &&
            ((TargetedDisguise) getDisguise()).canSee((Player) getDisguise().getEntity());
    }

    @Override
    public boolean isTinyFigureScaleable() {
        return getDisguise().canScaleDisguise() && getDisguise().isSelfDisguiseVisible() && getDisguise().isTallSelfDisguisesScaling();
    }

    @Override
    public boolean isScalePlayerToDisguise() {
        return getDisguise().isScalePlayerToDisguise();
    }

    @Override
    public boolean isTallDisguise() {
        return DisguiseUtilities.isTallDisguise(getDisguise());
    }

    private static AttributeModifier getAttributeModifier(double personalPlayerScaleAttribute) {
        if (!NmsVersion.v1_21_R1.isSupported()) {
            return new AttributeModifier(DisguiseUtilities.getSelfDisguiseScaleUUID(),
                DisguiseUtilities.getSelfDisguiseScaleNamespace().toString(), personalPlayerScaleAttribute,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.ANY);
        }

        return new AttributeModifier(DisguiseUtilities.getSelfDisguiseScaleNamespace(), personalPlayerScaleAttribute,
            AttributeModifier.Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.ANY);
    }

    @Override
    public double getUnscaledHeight() {
        return getDisguise().getHeight();
    }

    @Override
    public double getDisguiseScale() {
        return getDisguise().getDisguiseScale();
    }
}
