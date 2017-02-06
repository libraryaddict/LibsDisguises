package me.libraryaddict.disguise.disguisetypes.watchers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedAttribute;
import com.comphenix.protocol.wrappers.WrappedAttribute.Builder;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.ReflectionManager;

public class LivingWatcher extends FlagWatcher {
    static Map<Integer, Object> list = new HashMap<>();
    static Method getId;

    static {
        try {
            getId = ReflectionManager.getNmsMethod("MobEffectList", "getId", ReflectionManager.getNmsClass("MobEffectList"));
            Object REGISTRY = ReflectionManager.getNmsField("MobEffectList", "REGISTRY").get(null);

            for (Object next : ((Iterable) REGISTRY)) {
                int id = (int) getId.invoke(null, next);
                list.put(id, next);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private double maxHealth;
    private boolean maxHealthSet;
    private HashSet<Integer> potionEffects = new HashSet<>();

    public LivingWatcher(Disguise disguise) {
        super(disguise);
    }

    public void addPotionEffect(PotionEffectType potionEffect) {
        if (!hasPotionEffect(potionEffect)) {
            removePotionEffect(potionEffect);
            potionEffects.add(potionEffect.getId());

            sendPotionEffects();
        }
    }

    @Override
    public LivingWatcher clone(Disguise disguise) {
        LivingWatcher clone = (LivingWatcher) super.clone(disguise);
        clone.potionEffects = (HashSet<Integer>) potionEffects.clone();
        clone.maxHealth = maxHealth;
        clone.maxHealthSet = maxHealthSet;

        return clone;
    }

    public float getHealth() {
        return (float) getData(MetaIndex.LIVING_HEALTH);
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public boolean isPotionParticlesAmbient() {
        return (boolean) getData(MetaIndex.LIVING_POTION_AMBIENT);
    }

    private int getPotions() {
        int m = 3694022;

        if (potionEffects.isEmpty()) {
            return m;
        }

        float f1 = 0.0F;
        float f2 = 0.0F;
        float f3 = 0.0F;
        float f4 = 0.0F;
        try {
            for (int localMobEffect : potionEffects) {
                int n = (Integer) getId.invoke(list.get(localMobEffect));
                f1 += (n >> 16 & 0xFF) / 255.0F;
                f2 += (n >> 8 & 0xFF) / 255.0F;
                f3 += (n & 0xFF) / 255.0F;
                f4 += 1.0F;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        f1 = f1 / f4 * 255.0F;
        f2 = f2 / f4 * 255.0F;
        f3 = f3 / f4 * 255.0F;

        return (int) f1 << 16 | (int) f2 << 8 | (int) f3;
    }

    public boolean hasPotionEffect(PotionEffectType type) {
        return potionEffects.contains(type.getId());
    }

    public boolean isMaxHealthSet() {
        return maxHealthSet;
    }

    public void removePotionEffect(PotionEffectType type) {
        if (potionEffects.contains(type.getId())) {
            potionEffects.remove(type.getId());
            sendPotionEffects();
        }
    }

    public void setPotionParticlesAmbient(boolean particles) {
        setData(MetaIndex.LIVING_POTION_AMBIENT, particles);
        sendData(MetaIndex.LIVING_POTION_AMBIENT);
    }

    private void sendPotionEffects() {
        setData(MetaIndex.LIVING_POTIONS, getPotions());
        sendData(MetaIndex.LIVING_POTIONS);
    }

    public void setHealth(float health) {
        setData(MetaIndex.LIVING_HEALTH, health);
        sendData(MetaIndex.LIVING_HEALTH);
    }

    public int getArrowsSticking() {
        return (int) getData(MetaIndex.LIVING_ARROWS);
    }

    public void setArrowsSticking(int arrowsNo) {
        setData(MetaIndex.LIVING_ARROWS, arrowsNo);
        sendData(MetaIndex.LIVING_ARROWS);
    }

    public void setMaxHealth(double newHealth) {
        this.maxHealth = newHealth;
        this.maxHealthSet = true;

        if (DisguiseAPI.isDisguiseInUse(getDisguise()) && getDisguise().getWatcher() == this) {
            PacketContainer packet = new PacketContainer(Server.UPDATE_ATTRIBUTES);

            List<WrappedAttribute> attributes = new ArrayList<>();

            Builder builder;
            builder = WrappedAttribute.newBuilder();
            builder.attributeKey("generic.maxHealth");
            builder.baseValue(getMaxHealth());
            builder.packet(packet);

            attributes.add(builder.build());

            Entity entity = getDisguise().getEntity();

            packet.getIntegers().write(0, entity.getEntityId());
            packet.getAttributeCollectionModifier().write(0, attributes);

            for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                }
                catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
