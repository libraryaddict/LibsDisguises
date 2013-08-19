package me.libraryaddict.disguise.DisguiseTypes;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.PacketsManager;
import me.libraryaddict.disguise.DisguiseTypes.Watchers.AgeableWatcher;
import me.libraryaddict.disguise.DisguiseTypes.Watchers.HorseWatcher;
import me.libraryaddict.disguise.DisguiseTypes.Watchers.ZombieWatcher;
import net.minecraft.server.v1_6_R2.EntityAgeable;
import net.minecraft.server.v1_6_R2.EntityInsentient;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.EntityTrackerEntry;
import net.minecraft.server.v1_6_R2.WorldServer;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;

public class Disguise {
    private static DisguiseAPI disguiseAPI = new DisguiseAPI();
    private static JavaPlugin plugin;
    private DisguiseType disguiseType;
    private org.bukkit.entity.Entity entity;
    private boolean hearSelfDisguise = DisguiseAPI.canHearSelfDisguise();
    private boolean replaceSounds;
    private BukkitRunnable runnable;
    private boolean velocitySent = DisguiseAPI.isVelocitySent();
    private boolean viewSelfDisguise = DisguiseAPI.isViewDisguises();
    private FlagWatcher watcher;

    protected Disguise(DisguiseType newType, boolean doSounds) {
        // Set the disguise type
        disguiseType = newType;
        // Set the option to replace the sounds
        setReplaceSounds(doSounds);
        try {
            // Construct the FlagWatcher from the stored class
            setWatcher((FlagWatcher) getType().getWatcherClass().getConstructor(Disguise.class).newInstance(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Set the disguise if its a baby or not
        if (this instanceof MobDisguise && !((MobDisguise) this).isAdult()) {
            if (getWatcher() instanceof AgeableWatcher)
                getWatcher().setValue(12, -24000);
            else if (getWatcher() instanceof ZombieWatcher)
                getWatcher().setValue(12, (byte) 1);
        }
        // If the disguise type is a wither, set the flagwatcher value for the skeleton to a wither skeleton
        if (getType() == DisguiseType.WITHER_SKELETON)
            getWatcher().setValue(13, (byte) 1);
        // Else if its a zombie, but the disguise type is a zombie villager. Set the value.
        else if (getType() == DisguiseType.ZOMBIE_VILLAGER)
            getWatcher().setValue(13, (byte) 1);
        // Else if its a horse. Set the horse watcher type
        else if (getWatcher() instanceof HorseWatcher) {
            try {
                Variant horseType = Variant.valueOf(getType().name());
                getWatcher().setValue(19, (byte) horseType.ordinal());
            } catch (Exception ex) {
                // Ok.. So it aint a horse
            }
        }
        double fallSpeed = 0.0050;
        boolean movement = false;
        boolean alwaysSend = false;
        switch (getType()) {
        case EGG:
        case ENDER_PEARL:
        case BAT:
        case EXPERIENCE_ORB:
        case FIREBALL:
        case SMALL_FIREBALL:
        case SNOWBALL:
        case SPLASH_POTION:
        case THROWN_EXP_BOTTLE:
        case WITHER_SKULL:
            alwaysSend = true;
            break;
        default:
            break;
        }
        switch (getType()) {
        case FIREWORK:
            fallSpeed = -0.040;
            break;
        case EGG:
        case ENDER_PEARL:
        case ENDER_SIGNAL:
        case FIREBALL:
        case SMALL_FIREBALL:
        case SNOWBALL:
        case SPLASH_POTION:
        case THROWN_EXP_BOTTLE:
            fallSpeed = 0.0005;
            break;
        case WITHER_SKULL:
            fallSpeed = 0.000001D;
            break;
        case ARROW:
        case BOAT:
        case ENDER_CRYSTAL:
        case ENDER_DRAGON:
        case GHAST:
        case ITEM_FRAME:
        case MINECART:
        case MINECART_CHEST:
        case MINECART_FURNACE:
        case MINECART_HOPPER:
        case MINECART_MOB_SPAWNER:
        case MINECART_TNT:
        case PAINTING:
        case PLAYER:
        case SQUID:
            fallSpeed = 0;
            break;
        case DROPPED_ITEM:
        case EXPERIENCE_ORB:
        case MAGMA_CUBE:
        case PRIMED_TNT:
            fallSpeed = 0.2;
            movement = true;
            break;
        case WITHER:
        case FALLING_BLOCK:
            fallSpeed = 0.04;
            break;
        case SPIDER:
        case CAVE_SPIDER:
            fallSpeed = 0.0040;
            break;

        default:
            break;
        }
        final boolean sendMovementPacket = movement;
        final double vectorY = fallSpeed;
        final boolean alwaysSendVelocity = alwaysSend;
        // A scheduler to clean up any unused disguises.
        runnable = new BukkitRunnable() {
            private int i = 0;

            public void run() {
                // If entity is no longer valid. Remove it.
                if (!((CraftEntity) entity).getHandle().valid) {
                    DisguiseAPI.undisguiseToAll(entity);
                } else {
                    // If the disguise type is tnt, we need to resend the entity packet else it will turn invisible
                    if (getType() == DisguiseType.PRIMED_TNT) {
                        i++;
                        if (i % 40 == 0) {
                            i = 0;
                            List<Player> players = new ArrayList<Player>();
                            for (EntityPlayer p : getPerverts())
                                players.add(p.getBukkitEntity());
                            ProtocolLibrary.getProtocolManager().updateEntity(getEntity(), players);
                        }
                    }
                    // If the vectorY isn't 0. Cos if it is. Then it doesn't want to send any vectors.
                    // If this disguise has velocity sending enabled and the entity is flying.
                    if (vectorY != 0 && isVelocitySent() && (alwaysSendVelocity || !entity.isOnGround())) {
                        Vector vector = entity.getVelocity();
                        // If the entity doesn't have velocity changes already
                        if (vector.getY() != 0 && !(vector.getY() < 0 && alwaysSendVelocity && entity.isOnGround())) {
                            return;
                        }
                        PacketContainer lookPacket = null;
                        PacketContainer selfLookPacket = null;
                        if (getType() == DisguiseType.WITHER_SKULL) {
                            lookPacket = new PacketContainer(Packets.Server.ENTITY_LOOK);
                            StructureModifier<Object> mods = lookPacket.getModifier();
                            mods.write(0, entity.getEntityId());
                            Location loc = entity.getLocation();
                            mods.write(
                                    4,
                                    PacketsManager.getYaw(getType(), DisguiseType.getType(entity.getType()),
                                            (byte) Math.floor(loc.getYaw() * 256.0F / 360.0F)));
                            mods.write(5, (byte) Math.floor(loc.getPitch() * 256.0F / 360.0F));
                            selfLookPacket = lookPacket.shallowClone();
                        }
                        for (EntityPlayer player : getPerverts()) {
                            PacketContainer packet = new PacketContainer(Packets.Server.ENTITY_VELOCITY);
                            StructureModifier<Object> mods = packet.getModifier();
                            if (entity == player.getBukkitEntity()) {
                                if (!viewSelfDisguise())
                                    continue;
                                if (selfLookPacket != null) {
                                    try {
                                        ProtocolLibrary.getProtocolManager().sendServerPacket(player.getBukkitEntity(),
                                                selfLookPacket, false);
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                                mods.write(0, DisguiseAPI.getFakeDisguise(entity.getEntityId()));
                            } else
                                mods.write(0, entity.getEntityId());
                            mods.write(1, (int) (vector.getX() * 8000));
                            mods.write(2, (int) (8000 * (vectorY * (double) player.ping * 0.069)));
                            mods.write(3, (int) (vector.getZ() * 8000));
                            try {
                                if (lookPacket != null)
                                    ProtocolLibrary.getProtocolManager().sendServerPacket(player.getBukkitEntity(), lookPacket,
                                            false);
                                ProtocolLibrary.getProtocolManager().sendServerPacket(player.getBukkitEntity(), packet, false);
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                        // If we need to send more packets because else it still 'sinks'
                        if (sendMovementPacket) {
                            PacketContainer packet = new PacketContainer(Packets.Server.REL_ENTITY_MOVE);
                            StructureModifier<Object> mods = packet.getModifier();
                            mods.write(0, entity.getEntityId());
                            for (EntityPlayer player : getPerverts()) {
                                if (DisguiseAPI.isViewDisguises() || entity != player) {
                                    try {
                                        ProtocolLibrary.getProtocolManager().sendServerPacket(player.getBukkitEntity(), packet);
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    public boolean canHearSelfDisguise() {
        return hearSelfDisguise;
    }

    public Disguise clone() {
        Disguise disguise = new Disguise(getType(), replaceSounds());
        disguise.setViewSelfDisguise(viewSelfDisguise());
        return disguise;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Disguise other = (Disguise) obj;
        if (disguiseType != other.disguiseType)
            return false;
        if (hearSelfDisguise != other.hearSelfDisguise)
            return false;
        if (replaceSounds != other.replaceSounds)
            return false;
        if (velocitySent != other.velocitySent)
            return false;
        if (viewSelfDisguise != other.viewSelfDisguise)
            return false;
        if (!watcher.equals(other.watcher))
            return false;
        return true;
    }

    /**
     * Get the disguised entity
     */
    public org.bukkit.entity.Entity getEntity() {
        return entity;
    }

    /**
     * Get all EntityPlayers who have this entity in their Entity Tracker
     */
    protected EntityPlayer[] getPerverts() {
        EntityTrackerEntry entry = (EntityTrackerEntry) ((WorldServer) ((CraftEntity) entity).getHandle().world).tracker.trackedEntities
                .get(entity.getEntityId());
        if (entry != null) {
            EntityPlayer[] players = (EntityPlayer[]) entry.trackedPlayers.toArray(new EntityPlayer[entry.trackedPlayers.size()]);
            return players;
        }
        return new EntityPlayer[0];
    }

    /**
     * Get the disguise type
     */
    public DisguiseType getType() {
        return disguiseType;
    }

    /**
     * Get the flag watcher
     */
    public FlagWatcher getWatcher() {
        return watcher;
    }

    public boolean isMiscDisguise() {
        return this instanceof MiscDisguise;
    }

    public boolean isMobDisguise() {
        return this instanceof MobDisguise;
    }

    public boolean isPlayerDisguise() {
        return this instanceof PlayerDisguise;
    }

    public boolean isVelocitySent() {
        return velocitySent;
    }

    /**
     * Removes the disguise and undisguises the entity if its using this disguise. This doesn't fire a UndisguiseEvent
     */
    public void removeDisguise() {
        // Why the hell can't I safely check if its running?!?!
        try {
            runnable.cancel();
        } catch (Exception ex) {
        }
        HashMap<Integer, Disguise> disguises = disguiseAPI.getDisguises();
        // If this disguise has a entity set
        if (getEntity() != null) {
            // If the entity is valid
            if (((CraftEntity) getEntity()).getHandle().valid) {
                // If this disguise is active
                if (disguises.containsKey(getEntity().getEntityId()) && disguises.get(getEntity().getEntityId()) == this) {
                    // Now remove the disguise from the current disguises.
                    disguises.remove(getEntity().getEntityId());
                    // Gotta do reflection, copy code or open up calls.
                    // Reflection is the cleanest?
                    if (entity instanceof Player) {
                        disguiseAPI.removeVisibleDisguise((Player) entity);
                    }
                    // Better refresh the entity to undisguise it
                    disguiseAPI.refreshWatchingPlayers(getEntity());
                }
            }
        } else {
            // Loop through the disguises because it could be used with a unknown entity id.
            Iterator<Integer> itel = disguises.keySet().iterator();
            while (itel.hasNext()) {
                int id = itel.next();
                if (disguises.get(id) == this) {
                    itel.remove();
                }
            }
        }
    }

    public boolean replaceSounds() {
        return replaceSounds;
    }

    /**
     * Set the entity of the disguise. Only used for internal things.
     */
    public void setEntity(final org.bukkit.entity.Entity entity) {
        if (this.entity != null)
            throw new RuntimeException("This disguise is already in use! Try .clone()");
        this.entity = entity;
        setupWatcher();
        runnable.runTaskTimer(plugin, 1, 1);
    }

    public void setHearSelfDisguise(boolean hearSelfDisguise) {
        this.hearSelfDisguise = hearSelfDisguise;
    }

    public void setReplaceSounds(boolean areSoundsReplaced) {
        replaceSounds = areSoundsReplaced;
    }

    /**
     * Sets up the FlagWatcher with the entityclass, it creates all the data it needs to prevent conflicts when sending the
     * datawatcher.
     */
    private void setupWatcher() {
        Class disguiseClass = Values.getEntityClass(getType());
        HashMap<Integer, Object> disguiseValues = Values.getMetaValues(getType());
        HashMap<Integer, Object> entityValues = Values.getMetaValues(DisguiseType.getType(entity.getType()));
        // Start from 2 as they ALL share 0 and 1
        for (int dataNo = 2; dataNo <= 31; dataNo++) {
            // If the watcher already set a metadata on this
            if (getWatcher().getValue(dataNo, null) != null) {
                // Better check that the value is stable.
                if (disguiseValues.containsKey(dataNo)
                        && getWatcher().getValue(dataNo, null).getClass() == disguiseValues.get(dataNo).getClass()) {
                    // The classes are the same. The client "shouldn't" crash.
                    continue;
                }
            }
            // If neither of them touch it
            if (!entityValues.containsKey(dataNo) && !disguiseValues.containsKey(dataNo))
                continue;
            // If the disguise has this, but not the entity. Then better set it!
            if (!entityValues.containsKey(dataNo) && disguiseValues.containsKey(dataNo)) {
                getWatcher().setValue(dataNo, disguiseValues.get(dataNo));
                continue;
            }
            // Else if the disguise doesn't have it. But the entity does. Better remove it!
            if (entityValues.containsKey(dataNo) && !disguiseValues.containsKey(dataNo)) {
                getWatcher().setValue(dataNo, null);
                continue;
            }
            // Since they both share it. Time to check if its from something they extend.
            // Better make this clear before I compare the values because some default values are different!
            // Entity is 0 & 1 - But we aint gonna be checking that
            // EntityAgeable is 16
            // EntityInsentient is 10 & 11
            // EntityZombie is 12 & 13 & 14 - But it overrides other values and another check already does this.
            // EntityLiving is 6 & 7 & 8 & 9

            // Lets use switch
            Class baseClass = null;
            switch (dataNo) {
            case 6:
            case 7:
            case 8:
            case 9:
                baseClass = EntityLiving.class;
                break;
            case 10:
            case 11:
                baseClass = EntityInsentient.class;
                break;
            case 16:
                baseClass = EntityAgeable.class;
                break;
            default:
                break;
            }
            Class entityClass = ((CraftEntity) entity).getHandle().getClass();
            // If they both extend the same base class. They OBVIOUSLY share the same datavalue. Right..?
            if (baseClass != null && baseClass.isAssignableFrom(disguiseClass) && baseClass.isAssignableFrom(entityClass))
                continue;

            // So they don't extend a basic class.
            // Maybe if I check that they extend each other..
            // Seeing as I only store the finished forms of entitys. This should raise no problems and allow for more shared
            // datawatchers.
            if (entityClass.isAssignableFrom(disguiseClass) || disguiseClass.isAssignableFrom(entityClass))
                continue;
            // Well I can't find a reason I should leave it alone. They will probably conflict.
            // Time to set the value to the disguises value so no conflicts!
            getWatcher().setValue(dataNo, disguiseValues.get(dataNo));
        }
    }

    public void setVelocitySent(boolean sendVelocity) {
        this.velocitySent = sendVelocity;
    }

    /**
     * Can the disguised view himself as the disguise
     */
    public void setViewSelfDisguise(boolean viewSelfDisguise) {
        if (this.viewSelfDisguise != viewSelfDisguise) {
            this.viewSelfDisguise = viewSelfDisguise;
            if (getEntity() != null && getEntity() instanceof Player) {
                if (DisguiseAPI.getDisguise(getEntity()) == this) {
                    if (viewSelfDisguise) {
                        disguiseAPI.setupFakeDisguise(this);
                    } else
                        disguiseAPI.removeVisibleDisguise((Player) getEntity());
                }
            }
        }
    }

    public void setWatcher(FlagWatcher newWatcher) {
        watcher = newWatcher;
    }

    /**
     * Can the disguised view himself as the disguise
     */
    public boolean viewSelfDisguise() {
        return viewSelfDisguise;
    }
}