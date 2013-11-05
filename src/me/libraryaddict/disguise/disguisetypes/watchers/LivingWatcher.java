package me.libraryaddict.disguise.disguisetypes.watchers;

import java.util.HashSet;
import java.util.Iterator;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import net.minecraft.server.v1_6_R3.MobEffect;
import net.minecraft.server.v1_6_R3.PotionBrewer;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LivingWatcher extends FlagWatcher {
    private HashSet<MobEffect> potionEffects = new HashSet<MobEffect>();

    public LivingWatcher(Disguise disguise) {
        super(disguise);
    }

    public void addPotionEffect(PotionEffect potionEffect) {
        if (hasPotionEffect(potionEffect.getType()))
            removePotionEffect(potionEffect.getType());
        new MobEffect(potionEffect.getType().getId(), potionEffect.getDuration(), potionEffect.getAmplifier());
        sendPotionEffects();
    }

    @Override
    public LivingWatcher clone(Disguise disguise) {
        LivingWatcher clone = (LivingWatcher) super.clone(disguise);
        clone.potionEffects = (HashSet<MobEffect>) potionEffects.clone();
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

    public boolean hasCustomName() {
        return getCustomName().length() > 0;
    }

    public boolean hasPotionEffect(PotionEffectType type) {
        for (MobEffect effect : potionEffects)
            if (effect.getEffectId() == type.getId())
                return true;
        return false;
    }

    public boolean isCustomNameVisible() {
        return (Byte) getValue(11, (byte) 0) == 1;
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
        setValue(8, (byte) (particles ? 1 : 0));
        sendData(8);
    }

    private void sendPotionEffects() {
        setValue(7, PotionBrewer.a(potionEffects));
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
