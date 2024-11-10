package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleColorData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodGroupType;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodOnlyUsedBy;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;
import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class LivingWatcher extends FlagWatcher {
    @Getter
    private double maxHealth;
    @Getter
    private boolean maxHealthSet;
    private HashSet<String> potionEffects = new HashSet<>();
    @Getter
    private boolean[] modifiedLivingAnimations = new boolean[3];
    private Double viewScale;
    private boolean isScaleSet;

    public LivingWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    public LivingWatcher clone(Disguise disguise) {
        LivingWatcher clone = (LivingWatcher) super.clone(disguise);
        clone.potionEffects = (HashSet<String>) potionEffects.clone();
        clone.maxHealth = maxHealth;
        clone.maxHealthSet = maxHealthSet;
        clone.modifiedLivingAnimations = Arrays.copyOf(modifiedLivingAnimations, modifiedLivingAnimations.length);
        clone.viewScale = viewScale;

        return clone;
    }

    @NmsAddedIn(NmsVersion.v1_20_R4)
    public Double getScale() {
        return viewScale;
    }

    @NmsAddedIn(NmsVersion.v1_20_R4)
    public void setScale(Double viewScale) {
        if (!NmsVersion.v1_20_R4.isSupported()) {
            return;
        }

        // Clamping, the actual values are 0.06 to 16, but why do we need to force it?
        if (viewScale != null) {
            if (viewScale < 0) {
                viewScale = 0D;
            } else if (viewScale > 100) {
                viewScale = 100D;
            }
        }

        this.viewScale = viewScale;

        if (getDisguise() == null || !getDisguise().isDisguiseInUse() || getDisguise().getWatcher() != this) {
            return;
        }

        updateNameHeight();
        double scaleToSend;

        if (getScale() != null) {
            scaleToSend = getScale();
        } else {
            scaleToSend = DisguiseUtilities.getEntityScaleWithoutLibsDisguises(getDisguise().getEntity());
        }

        Entity entity = getDisguise().getEntity();

        for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
            double toSend = player == entity && DisguiseConfig.isTallSelfDisguisesScaling() ?
                Math.min(getDisguise().getInternals().getSelfDisguiseTallScaleMax(), scaleToSend) : scaleToSend;

            WrapperPlayServerUpdateAttributes.Property property =
                new WrapperPlayServerUpdateAttributes.Property(Attributes.GENERIC_SCALE, toSend, new ArrayList<>());

            WrapperPlayServerUpdateAttributes packet = new WrapperPlayServerUpdateAttributes(
                player == getDisguise().getEntity() ? DisguiseAPI.getSelfDisguiseId() : getDisguise().getEntity().getEntityId(),
                Collections.singletonList(property));

            if (player == getDisguise().getEntity()) {
                PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
            } else {
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
            }
        }
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public Vector3i getBedPosition() {
        return getData(MetaIndex.LIVING_BED_POSITION).orElse(null);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setBedPosition(Vector3i blockPosition) {
        Optional<Vector3i> optional;

        if (blockPosition != null) {
            optional = Optional.of(blockPosition);
        } else {
            optional = Optional.empty();
        }

        sendData(MetaIndex.LIVING_BED_POSITION, optional);
    }

    public float getHealth() {
        return getData(MetaIndex.LIVING_HEALTH);
    }

    public void setHealth(float health) {
        sendData(MetaIndex.LIVING_HEALTH, health);
    }

    /*@NmsAddedIn(val = NmsVersion.v1_13)
    public MainHand getMainHand() {
        return getHandFlag(0) ? MainHand.RIGHT : MainHand.LEFT;
    }

    @NmsAddedIn(val = NmsVersion.v1_13)
    public void setMainHand(MainHand hand) {
        setHandFlag(0, hand == MainHand.RIGHT);
    }*/

    private boolean getHandFlag(int byteValue) {
        return (getData(MetaIndex.LIVING_META) & 1 << byteValue) != 0;
    }

    private void setHandFlag(int byteValue, boolean flag) {
        byte b0 = getData(MetaIndex.LIVING_META);
        modifiedLivingAnimations[byteValue] = true;

        if (flag) {
            b0 = (byte) (b0 | 1 << byteValue);
        } else {
            b0 = (byte) (b0 & ~(1 << byteValue));
        }

        sendData(MetaIndex.LIVING_META, b0);
    }

    private boolean isMainHandUsed() {
        return !getHandFlag(1);
    }

    private void setHandInUse(boolean mainHand) {
        if (isMainHandUsed() == mainHand) {
            return;
        }

        setHandFlag(1, !mainHand);
    }

    @Override
    @NmsAddedIn(NmsVersion.v1_13)
    public boolean isMainHandRaised() {
        return isMainHandUsed() && getHandFlag(0);
    }

    @Override
    @NmsAddedIn(NmsVersion.v1_13)
    @MethodOnlyUsedBy(value = {}, group = MethodGroupType.HOLDABLE)
    public void setMainHandRaised(boolean setRightClicking) {
        setHandInUse(true);

        setHandFlag(0, setRightClicking);
    }

    @NmsAddedIn(NmsVersion.v1_13)
    public boolean isOffhandRaised() {
        return !isMainHandUsed() && getHandFlag(0);
    }

    @NmsAddedIn(NmsVersion.v1_13)
    @MethodOnlyUsedBy(value = {}, group = MethodGroupType.HOLDABLE)
    public void setOffhandRaised(boolean setLeftClicking) {
        setHandInUse(false);

        setHandFlag(0, setLeftClicking);
    }

    @NmsAddedIn(NmsVersion.v1_13)
    public boolean isSpinning() {
        return getHandFlag(2);
    }

    @NmsAddedIn(NmsVersion.v1_13)
    public void setSpinning(boolean setSpinning) {
        setHandFlag(2, setSpinning);
    }

    public void setMaxHealth(double newHealth) {
        this.maxHealth = newHealth;
        this.maxHealthSet = true;

        if (!getDisguise().isDisguiseInUse() || getDisguise().getWatcher() != this) {
            return;
        }

        for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
            WrapperPlayServerUpdateAttributes.Property property =
                new WrapperPlayServerUpdateAttributes.Property(Attributes.GENERIC_MAX_HEALTH, getMaxHealth(), new ArrayList<>());
            WrapperPlayServerUpdateAttributes packet = new WrapperPlayServerUpdateAttributes(
                player == getDisguise().getEntity() ? DisguiseAPI.getSelfDisguiseId() : getDisguise().getEntity().getEntityId(),
                Collections.singletonList(property));

            if (player == getDisguise().getEntity()) {
                PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
            } else {
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
            }
        }
    }

    public boolean isPotionParticlesAmbient() {
        return getData(MetaIndex.LIVING_POTION_AMBIENT);
    }

    public void setPotionParticlesAmbient(boolean particles) {
        sendData(MetaIndex.LIVING_POTION_AMBIENT, particles);
    }

    @NmsAddedIn(NmsVersion.v1_20_R4)
    public void addParticle(Particle<? extends ParticleData> particle) {
        getData(MetaIndex.LIVING_PARTICLES).add(particle);
        sendData(MetaIndex.LIVING_PARTICLES);
    }

    @SafeVarargs
    @NmsAddedIn(NmsVersion.v1_20_R4)
    public final void removeParticles(Particle<? extends ParticleData>... particles) {
        for (Particle<? extends ParticleData> particle : particles) {
            getData(MetaIndex.LIVING_PARTICLES).remove(particle);
        }

        sendData(MetaIndex.LIVING_PARTICLES);
    }

    @NmsAddedIn(NmsVersion.v1_20_R4)
    public List<Particle<? extends ParticleData>> getParticles() {
        return Collections.unmodifiableList(getData(MetaIndex.LIVING_PARTICLES));
    }

    public Color getParticlesColor() {
        if (!NmsVersion.v1_20_R4.isSupported()) {
            return Color.fromRGB(getData(MetaIndex.LIVING_POTIONS));
        }

        return getData(MetaIndex.LIVING_PARTICLES).stream().filter(p -> p.getType() == ParticleTypes.ENTITY_EFFECT).findAny()
            .map(p -> Color.fromRGB(((ParticleColorData) p.getData()).getColor())).orElse(Color.BLACK);
    }

    public void setParticlesColor(Color color) {
        potionEffects.clear();

        if (NmsVersion.v1_20_R4.isSupported()) {
            List<Particle<?>> particles = new ArrayList<>(getData(MetaIndex.LIVING_PARTICLES));
            particles.removeIf(d -> d.getType() == ParticleTypes.ENTITY_EFFECT);
            particles.add(new Particle<>(ParticleTypes.ENTITY_EFFECT, new ParticleColorData(color.asRGB())));

            sendData(MetaIndex.LIVING_PARTICLES, particles);
        } else {
            sendData(MetaIndex.LIVING_POTIONS, color.asRGB());
        }
    }

    private int getPotions() {
        if (potionEffects.isEmpty()) {
            return 0;
        }

        ArrayList<Color> colors = new ArrayList<>();

        for (String typeId : potionEffects) {
            PotionEffectType type = PotionEffectType.getByName(typeId);

            if (type == null) {
                continue;
            }

            Color color = type.getColor();

            if (color == null) {
                continue;
            }

            colors.add(color);
        }

        if (colors.isEmpty()) {
            return 0;
        }

        Color color = colors.remove(0);

        return color.mixColors(colors.toArray(new Color[0])).asRGB();
    }

    @Deprecated
    public boolean hasPotionEffect(PotionEffectType type) {
        return potionEffects.contains(type.getName());
    }

    @Deprecated
    public PotionEffectType[] getPotionEffects() {
        PotionEffectType[] effects = new PotionEffectType[potionEffects.size()];

        int i = 0;

        for (String potionEffect : potionEffects) {
            PotionEffectType type = PotionEffectType.getByName(potionEffect);

            effects[i++] = type;
        }

        return effects;
    }

    @Deprecated
    @NmsRemovedIn(NmsVersion.v1_20_R4)
    public void addPotionEffect(PotionEffectType potionEffect) {
        if (!hasPotionEffect(potionEffect)) {
            potionEffects.add(potionEffect.getName());
        }

        sendPotionEffects();
    }

    @Deprecated
    @NmsRemovedIn(NmsVersion.v1_20_R4)
    public void removePotionEffect(PotionEffectType potionEffect) {
        if (hasPotionEffect(potionEffect)) {
            potionEffects.remove(potionEffect.getId());
        }

        sendPotionEffects();
    }

    private void sendPotionEffects() {
        sendData(MetaIndex.LIVING_POTIONS, getPotions());
    }

    public int getArrowsSticking() {
        return getData(MetaIndex.LIVING_ARROWS);
    }

    @MethodOnlyUsedBy(value = {DisguiseType.PLAYER})
    public void setArrowsSticking(int arrowsNo) {
        sendData(MetaIndex.LIVING_ARROWS, Math.max(0, Math.min(127, arrowsNo)));
    }

    @Override
    protected byte addEntityAnimations(MetaIndex index, byte originalValue, byte entityValue) {
        if (index != MetaIndex.LIVING_META) {
            return super.addEntityAnimations(index, originalValue, entityValue);
        }

        for (int i = 0; i < 3; i++) {
            if ((entityValue & 1 << i) != 0 && !modifiedLivingAnimations[i]) {
                originalValue = (byte) (originalValue | 1 << i);
            }
        }

        return originalValue;
    }

    @Deprecated
    @NmsAddedIn(NmsVersion.v1_12)
    public boolean isRightClicking() {
        return isMainHandRaised();
    }

    @Deprecated
    @NmsAddedIn(NmsVersion.v1_12)
    @MethodOnlyUsedBy(value = {}, group = MethodGroupType.HOLDABLE)
    public void setRightClicking(boolean rightClicking) {
        setMainHandRaised(rightClicking);
    }
}
