package me.libraryaddict.disguise.DisguiseTypes;

import net.minecraft.server.v1_5_R3.Entity;
import net.minecraft.server.v1_5_R3.EntityAgeable;
import net.minecraft.server.v1_5_R3.EntityLiving;
import net.minecraft.server.v1_5_R3.EntityZombie;
import net.minecraft.server.v1_5_R3.World;

import org.bukkit.Location;

public class MobDisguise extends Disguise {

    private boolean adult;

    public MobDisguise(DisguiseType disguiseType, boolean isAdult) {
        super(disguiseType);
        adult = isAdult;
    }

    protected EntityLiving getEntityLiving(World w, Location loc, int id) {
        Entity entity = getEntity(w, loc, id);
        if (!adult) {
            if (entity instanceof EntityAgeable)
                ((EntityAgeable) entity).setAge(-24000);
            else if (entity instanceof EntityZombie)
                ((EntityZombie) entity).setBaby(true);
        }
        if (entity instanceof EntityLiving)
            return (EntityLiving) entity;
        return null;
    }

    public boolean isAdult() {
        return adult;
    }

    public void setAdult(boolean setAdult) {
        adult = setAdult;
    }
}