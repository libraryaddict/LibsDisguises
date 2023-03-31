package me.libraryaddict.disguise.disguisetypes.watchers;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedAttribute;
import com.comphenix.protocol.wrappers.WrappedAttribute.Builder;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodGroupType;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodOnlyUsedBy;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class LivingWatcher extends FlagWatcher {
    private double maxHealth;
    private boolean maxHealthSet;
    private HashSet<String> potionEffects = new HashSet<>();
    @Getter
    private boolean[] modifiedLivingAnimations = new boolean[3];

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

        return clone;
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public BlockPosition getBedPosition() {
        return getData(MetaIndex.LIVING_BED_POSITION).orElse(null);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setBedPosition(BlockPosition blockPosition) {
        Optional<BlockPosition> optional;

        if (blockPosition != null) {
            optional = Optional.of(blockPosition);
        } else {
            optional = Optional.empty();
        }

        setData(MetaIndex.LIVING_BED_POSITION, optional);
        sendData(MetaIndex.LIVING_BED_POSITION);
    }

    public float getHealth() {
        return getData(MetaIndex.LIVING_HEALTH);
    }

    public void setHealth(float health) {
        setData(MetaIndex.LIVING_HEALTH, health);
        sendData(MetaIndex.LIVING_HEALTH);
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
            setData(MetaIndex.LIVING_META, (byte) (b0 | 1 << byteValue));
        } else {
            setData(MetaIndex.LIVING_META, (byte) (b0 & ~(1 << byteValue)));
        }

        sendData(MetaIndex.LIVING_META);
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

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(double newHealth) {
        this.maxHealth = newHealth;
        this.maxHealthSet = true;

        if (DisguiseAPI.isDisguiseInUse(getDisguise()) && getDisguise().getWatcher() == this) {
            PacketContainer packet = new PacketContainer(Server.UPDATE_ATTRIBUTES);

            List<WrappedAttribute> attributes = new ArrayList<>();

            Builder builder;
            builder = WrappedAttribute.newBuilder();
            builder.attributeKey(NmsVersion.v1_16.isSupported() ? "generic.max_health" : "generic.maxHealth");
            builder.baseValue(getMaxHealth());
            builder.packet(packet);

            attributes.add(builder.build());

            Entity entity = getDisguise().getEntity();

            packet.getIntegers().write(0, entity.getEntityId());
            packet.getAttributeCollectionModifier().write(0, attributes);

            for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
                if (player == getDisguise().getEntity()) {
                    PacketContainer p = packet.shallowClone();
                    p.getIntegers().write(0, DisguiseAPI.getSelfDisguiseId());
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, p, false);
                } else {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                }
            }
        }
    }

    public boolean isPotionParticlesAmbient() {
        return getData(MetaIndex.LIVING_POTION_AMBIENT);
    }

    public void setPotionParticlesAmbient(boolean particles) {
        setData(MetaIndex.LIVING_POTION_AMBIENT, particles);
        sendData(MetaIndex.LIVING_POTION_AMBIENT);
    }

    public Color getParticlesColor() {
        int color = getData(MetaIndex.LIVING_POTIONS);
        return Color.fromRGB(color);
    }

    public void setParticlesColor(Color color) {
        potionEffects.clear();

        setData(MetaIndex.LIVING_POTIONS, color.asRGB());
        sendData(MetaIndex.LIVING_POTIONS);
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

    public boolean hasPotionEffect(PotionEffectType type) {
        return potionEffects.contains(type.getName());
    }

    public boolean isMaxHealthSet() {
        return maxHealthSet;
    }

    public PotionEffectType[] getPotionEffects() {
        PotionEffectType[] effects = new PotionEffectType[potionEffects.size()];

        int i = 0;

        Iterator<String> itel = potionEffects.iterator();

        while (itel.hasNext()) {
            PotionEffectType type = PotionEffectType.getByName(itel.next());

            effects[i++] = type;
        }

        return effects;
    }

    public void addPotionEffect(PotionEffectType potionEffect) {
        if (!hasPotionEffect(potionEffect)) {
            potionEffects.add(potionEffect.getName());
        }

        sendPotionEffects();
    }

    public void removePotionEffect(PotionEffectType potionEffect) {
        if (hasPotionEffect(potionEffect)) {
            potionEffects.remove(potionEffect.getId());
        }

        sendPotionEffects();
    }

    private void sendPotionEffects() {
        setData(MetaIndex.LIVING_POTIONS, getPotions());
        sendData(MetaIndex.LIVING_POTIONS);
    }

    public int getArrowsSticking() {
        return getData(MetaIndex.LIVING_ARROWS);
    }

    @MethodOnlyUsedBy(value = {DisguiseType.PLAYER})
    public void setArrowsSticking(int arrowsNo) {
        setData(MetaIndex.LIVING_ARROWS, Math.max(0, Math.min(127, arrowsNo)));
        sendData(MetaIndex.LIVING_ARROWS);
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
