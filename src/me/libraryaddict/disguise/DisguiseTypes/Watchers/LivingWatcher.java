package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import java.util.HashSet;
import java.util.Iterator;

import net.minecraft.server.v1_5_R3.MobEffect;
import net.minecraft.server.v1_5_R3.PotionBrewer;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

public class LivingWatcher extends FlagWatcher {
    private HashSet<MobEffect> potionEffects = new HashSet<MobEffect>();

    public LivingWatcher(int entityId) {
        super(entityId);
        setValue(5, "");
        setValue(6, (byte) 0);
    }

    public void addPotionEffect(PotionEffect potionEffect) {
        if (hasPotionEffect(potionEffect.getType()))
            removePotionEffect(potionEffect.getType());
        new MobEffect(potionEffect.getType().getId(), potionEffect.getDuration(), potionEffect.getAmplifier());
        sendPotionEffects();
    }

    public int getArrowsSticking() {
        return (Byte) getValue(10);
    }

    public String getCustomName() {
        return (String) getValue(5);
    }

    public boolean getPotionParticlesRemoved() {
        return (Byte) getValue(9) == 1;
    }

    public boolean hasCustomName() {
        return getCustomName().length() > 0;
    }

    public boolean hasPotionEffect(PotionEffectType type) {
        for (MobEffect effect : potionEffects)
            if (effect.getEffectId() == type.getId())
                return true;
        return false;
    }

    public void removePotionEffect(PotionEffectType type) {
        Iterator<MobEffect> itel = potionEffects.iterator();
        while (itel.hasNext()) {
            MobEffect effect = itel.next();
            if (effect.getEffectId() == type.getId()) {
                itel.remove();
                sendPotionEffects();
                return;
            }
        }
    }

    public void removePotionParticles(boolean particles) {
        if (particles != getPotionParticlesRemoved()) {
            setValue(9, (byte) (particles ? 1 : 0));
            sendData(9);
        }
    }

    private void sendPotionEffects() {
        setValue(8, PotionBrewer.a(potionEffects));
        sendData(8);
    }

    public void setArrowsSticking(int arrowsNo) {
        if (arrowsNo != getArrowsSticking()) {
            setValue(10, (byte) arrowsNo);
            sendData(10);
        }
    }

    public void setCustomName(String name) {
        if (!getCustomName().equals(name)) {
            setValue(5, name);
            sendData(5);
        }
    }

    public void setCustomNameVisible(boolean display) {
        if ((Byte) getValue(6) != (display ? 1 : 0)) {
            setValue(6, (byte) (display ? 1 : 0));
            sendData(6);
        }
    }

}
