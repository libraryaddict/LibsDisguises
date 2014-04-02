package me.libraryaddict.disguise.disguisetypes.watchers;

import java.lang.reflect.Method;
import java.util.HashSet;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.ReflectionManager;

import org.bukkit.potion.PotionEffectType;

public class LivingWatcher extends FlagWatcher {
    static Object[] list;
    static Method potionNo;
    static {
        try {
            Class mobEffectList = ReflectionManager.getNmsClass("MobEffectList");
            list = (Object[]) mobEffectList.getField("byId").get(null);
            for (Object obj : list) {
                if (obj != null) {
                    for (Method field : obj.getClass().getMethods()) {
                        if (field.getReturnType() == int.class) {
                            if ((Integer) field.invoke(obj) > 10000) {
                                potionNo = field;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private HashSet<Integer> potionEffects = new HashSet<Integer>();

    public LivingWatcher(Disguise disguise) {
        super(disguise);
    }

    public void addPotionEffect(PotionEffectType potionEffect) {
        if (hasPotionEffect(potionEffect))
            removePotionEffect(potionEffect);
        potionEffects.add(potionEffect.getId());
        sendPotionEffects();
    }

    @Override
    public LivingWatcher clone(Disguise disguise) {
        LivingWatcher clone = (LivingWatcher) super.clone(disguise);
        clone.potionEffects = (HashSet<Integer>) potionEffects.clone();
        return clone;
    }

    public String getCustomName() {
        return (String) getValue(10, "");
    }

    public float getHealth() {
        return (Float) getValue(6, 0F);
    }

    public boolean getPotionParticlesRemoved() {
        return (Byte) getValue(8, (byte) 0) == 1;
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
                int n = (Integer) potionNo.invoke(list[localMobEffect]);
                f1 += (n >> 16 & 0xFF) / 255.0F;
                f2 += (n >> 8 & 0xFF) / 255.0F;
                f3 += (n >> 0 & 0xFF) / 255.0F;
                f4 += 1.0F;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        f1 = f1 / f4 * 255.0F;
        f2 = f2 / f4 * 255.0F;
        f3 = f3 / f4 * 255.0F;

        return (int) f1 << 16 | (int) f2 << 8 | (int) f3;
    }

    public boolean hasCustomName() {
        return getCustomName().length() > 0;
    }

    public boolean hasPotionEffect(PotionEffectType type) {
        return potionEffects.contains(type.getId());
    }

    public boolean isCustomNameVisible() {
        return (Byte) getValue(11, (byte) 0) == 1;
    }

    public void removePotionEffect(PotionEffectType type) {
        if (potionEffects.contains(type.getId())) {
            potionEffects.remove(type.getId());
            sendPotionEffects();
        }
    }

    public void removePotionParticles(boolean particles) {
        setValue(8, (byte) (particles ? 1 : 0));
        sendData(8);
    }

    private void sendPotionEffects() {
        setValue(7, getPotions());
        sendData(7);
    }

    public void setCustomName(String name) {
        if (name.length() > 64)
            name = name.substring(0, 64);
        setValue(10, name);
        sendData(10);
    }

    public void setCustomNameVisible(boolean display) {
        setValue(11, (byte) (display ? 1 : 0));
        sendData(11);
    }

    public void setHealth(float health) {
        setValue(6, health);
        sendData(6);
    }

}
