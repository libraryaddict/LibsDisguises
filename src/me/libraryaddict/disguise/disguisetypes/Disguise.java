package me.libraryaddict.disguise.disguisetypes;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise.TargetType;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.HorseWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.PacketsManager;
import me.libraryaddict.disguise.utilities.ReflectionManager;
import me.libraryaddict.disguise.utilities.DisguiseValues;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;

public abstract class Disguise {
    private static JavaPlugin plugin;
    private DisguiseType disguiseType;
    private Entity entity;
    private boolean hearSelfDisguise = DisguiseAPI.isSelfDisguisesSoundsReplaced();
    private boolean hideArmorFromSelf = DisguiseAPI.isHidingArmorFromSelf();
    private boolean hideHeldItemFromSelf = DisguiseAPI.isHidingHeldItemFromSelf();
    private boolean modifyBoundingBox = DisguiseAPI.isModifyBoundingBox();
    private boolean replaceSounds = DisguiseAPI.isSoundEnabled();
    private BukkitRunnable velocityRunnable;
    private boolean velocitySent = DisguiseAPI.isVelocitySent();
    private boolean viewSelfDisguise = DisguiseAPI.isViewDisguises();
    private FlagWatcher watcher;

    @Deprecated
    public boolean canHearSelfDisguise() {
        return hearSelfDisguise;
    }

    @Override
    public abstract Disguise clone();

    /**
     * Seems I do this method so I can make cleaner constructors on disguises..
     */
    protected void createDisguise(DisguiseType newType, boolean doSounds) {
        if (getWatcher() != null)
            return;
        if (newType.getEntityType() == null) {
            throw new RuntimeException(
                    "DisguiseType "
                            + newType
                            + " was used in a futile attempt to construct a disguise, but this version of craftbukkit does not have that entity");
        }
        // Set the disguise type
        disguiseType = newType;
        // Set the option to replace the sounds
        setReplaceSounds(doSounds);
        // Get if they are a adult now..
        boolean isAdult = true;
        if (isMobDisguise()) {
            isAdult = ((MobDisguise) this).isAdult();
        }
        try {
            // Construct the FlagWatcher from the stored class
            setWatcher((FlagWatcher) getType().getWatcherClass().getConstructor(Disguise.class).newInstance(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Set the disguise if its a baby or not
        if (!isAdult) {
            if (getWatcher() instanceof AgeableWatcher) {
                ((AgeableWatcher) getWatcher()).setAdult(false);
            } else if (getWatcher() instanceof ZombieWatcher) {
                ((ZombieWatcher) getWatcher()).setAdult(false);
            }
        }
        // If the disguise type is a wither, set the flagwatcher value for the skeleton to a wither skeleton
        if (getType() == DisguiseType.WITHER_SKELETON) {
            getWatcher().setValue(13, (byte) 1);
        }
        // Else if its a zombie, but the disguise type is a zombie villager. Set the value.
        else if (getType() == DisguiseType.ZOMBIE_VILLAGER) {
            getWatcher().setValue(13, (byte) 1);
        }
        // Else if its a horse. Set the horse watcher type
        else if (getWatcher() instanceof HorseWatcher) {
            try {
                // Don't mess with this because Varient is something like ZombieHorse and so on.
                // Not something that a watcher needs to access.
                Variant horseType = Variant.valueOf(getType().name());
                getWatcher().setValue(19, (byte) horseType.ordinal());
            } catch (Exception ex) {
                // Ok.. So it aint a horse
            }
        }
        double fallSpeed = 0.0005;
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
            // case ITEM_FRAME:
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
        case PRIMED_TNT:
        case WITHER:
        case FALLING_BLOCK:
            fallSpeed = 0.04;
            break;
        case EXPERIENCE_ORB:
            fallSpeed = 0.0221;
            movement = true;
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
        final TargetedDisguise disguise = (TargetedDisguise) this;
        // A scheduler to clean up any unused disguises.
        velocityRunnable = new BukkitRunnable() {
            private int i = 0;

            public void run() {
                // If entity is no longer valid. Remove it.
                if (!getEntity().isValid()) {
                    DisguiseAPI.undisguiseToAll(getEntity());
                } else {
                    // If the disguise type is tnt, we need to resend the entity packet else it will turn invisible
                    if (getType() == DisguiseType.PRIMED_TNT || getType() == DisguiseType.FIREWORK) {
                        i++;
                        if (i % 40 == 0) {
                            i = 0;
                            DisguiseUtilities.refreshTrackers(disguise);
                            if (getEntity() instanceof Player && isSelfDisguiseVisible()) {
                                DisguiseUtilities.sendSelfDisguise((Player) getEntity());
                            }
                        }
                    }
                    // If the vectorY isn't 0. Cos if it is. Then it doesn't want to send any vectors.
                    // If this disguise has velocity sending enabled and the entity is flying.
                    if (vectorY != 0 && isVelocitySent() && (alwaysSendVelocity || !getEntity().isOnGround())) {
                        Vector vector = getEntity().getVelocity();
                        // If the entity doesn't have velocity changes already
                        if (vector.getY() != 0 && !(vector.getY() < 0 && alwaysSendVelocity && getEntity().isOnGround())) {
                            return;
                        }
                        if (getType() != DisguiseType.EXPERIENCE_ORB || !getEntity().isOnGround()) {
                            PacketContainer lookPacket = null;
                            if (getType() == DisguiseType.WITHER_SKULL) {
                                lookPacket = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
                                StructureModifier<Object> mods = lookPacket.getModifier();
                                mods.write(0, getEntity().getEntityId());
                                Location loc = getEntity().getLocation();
                                mods.write(
                                        4,
                                        PacketsManager.getYaw(getType(), getEntity().getType(),
                                                (byte) Math.floor(loc.getYaw() * 256.0F / 360.0F)));
                                mods.write(5, (byte) Math.floor(loc.getPitch() * 256.0F / 360.0F));
                                if (isSelfDisguiseVisible() && getEntity() instanceof Player) {
                                    PacketContainer selfLookPacket = lookPacket.shallowClone();
                                    selfLookPacket.getModifier().write(0, DisguiseAPI.getFakeDisguise(getEntity().getEntityId()));
                                    try {
                                        ProtocolLibrary.getProtocolManager().sendServerPacket((Player) getEntity(),
                                                selfLookPacket, false);
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            try {
                                Field ping = ReflectionManager.getNmsClass("EntityPlayer").getField("ping");
                                for (Player player : DisguiseUtilities.getPerverts(disguise)) {
                                    PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_VELOCITY);
                                    StructureModifier<Object> mods = packet.getModifier();
                                    if (getEntity() == player) {
                                        if (!isSelfDisguiseVisible()) {
                                            continue;
                                        }
                                        mods.write(0, DisguiseAPI.getFakeDisguise(getEntity().getEntityId()));
                                    } else {
                                        mods.write(0, getEntity().getEntityId());
                                    }
                                    mods.write(1, (int) (vector.getX() * 8000));
                                    mods.write(
                                            2,
                                            (int) (8000 * (vectorY * (double) ping.getInt(ReflectionManager.getNmsEntity(player)) * 0.069)));
                                    mods.write(3, (int) (vector.getZ() * 8000));
                                    if (lookPacket != null) {
                                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, lookPacket);
                                    }
                                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        // If we need to send more packets because else it still 'sinks'
                        if (sendMovementPacket) {
                            PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE);
                            StructureModifier<Object> mods = packet.getModifier();
                            mods.write(0, getEntity().getEntityId());
                            for (Player player : DisguiseUtilities.getPerverts(disguise)) {
                                if (DisguiseAPI.isViewDisguises() || getEntity() != player) {
                                    try {
                                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
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

    /**
     * Get the disguised entity
     */
    public Entity getEntity() {
        return entity;
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

    public boolean isHidingArmorFromSelf() {
        return hideArmorFromSelf;
    }

    public boolean isHidingHeldItemFromSelf() {
        return hideHeldItemFromSelf;
    }

    public boolean isMiscDisguise() {
        return false;
    }

    public boolean isMobDisguise() {
        return false;
    }

    public boolean isModifyBoundingBox() {
        return modifyBoundingBox;
    }

    public boolean isPlayerDisguise() {
        return false;
    }

    public boolean isSelfDisguiseSoundsReplaced() {
        return hearSelfDisguise;
    }

    /**
     * Can the disguised view himself as the disguise
     */
    public boolean isSelfDisguiseVisible() {
        return viewSelfDisguise;
    }

    public boolean isSoundsReplaced() {
        return replaceSounds;
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
            velocityRunnable.cancel();
        } catch (Exception ex) {
        }
        HashMap<Integer, HashSet<TargetedDisguise>> disguises = DisguiseUtilities.getDisguises();
        // If this disguise has a entity set
        if (getEntity() != null) {
            // If this disguise is active
            // Remove the disguise from the current disguises.
            if (DisguiseUtilities.removeDisguise((TargetedDisguise) this)) {
                if (getEntity() instanceof Player) {
                    DisguiseUtilities.removeSelfDisguise((Player) getEntity());
                }

                // If the entity is not dead
                if (getEntity().isValid()) {
                    // Better refresh the entity to undisguise it
                    DisguiseUtilities.refreshTrackers((TargetedDisguise) this);
                }
            }
        } else {
            // Loop through the disguises because it could be used with a unknown entity id.
            Iterator<Integer> itel = disguises.keySet().iterator();
            while (itel.hasNext()) {
                int id = itel.next();
                if (disguises.get(id).remove(this) && disguises.get(id).isEmpty()) {
                    itel.remove();
                }
            }
        }
    }

    @Deprecated
    public boolean replaceSounds() {
        return replaceSounds;
    }

    /**
     * Set the entity of the disguise. Only used for internal things.
     */
    public void setEntity(Entity entity) {
        if (this.getEntity() != null) {
            throw new RuntimeException("This disguise is already in use! Try .clone()");
        }
        this.entity = entity;
        setupWatcher();
        velocityRunnable.runTaskTimer(plugin, 1, 1);
    }

    public void setHearSelfDisguise(boolean hearSelfDisguise) {
        this.hearSelfDisguise = hearSelfDisguise;
    }

    public void setHideArmorFromSelf(boolean hideArmor) {
        this.hideArmorFromSelf = hideArmor;
        if (getEntity() instanceof Player) {
            ((Player) getEntity()).updateInventory();
        }
    }

    public void setHideHeldItemFromSelf(boolean hideHeldItem) {
        this.hideHeldItemFromSelf = hideHeldItem;
        if (getEntity() instanceof Player) {
            ((Player) getEntity()).updateInventory();
        }
    }

    public void setModifyBoundingBox(boolean modifyBox) {
        if (((TargetedDisguise) this).getDisguiseTarget() != TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS) {
            throw new RuntimeException(
                    "Cannot modify the bounding box of a disguise which is not TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS");
        }
        if (isModifyBoundingBox() != modifyBox) {
            this.modifyBoundingBox = modifyBox;
            if (DisguiseUtilities.isDisguiseInUse(this)) {
                DisguiseUtilities.doBoundingBox((TargetedDisguise) this);
            }
        }
    }

    public void setReplaceSounds(boolean areSoundsReplaced) {
        replaceSounds = areSoundsReplaced;
    }

    /**
     * Sets up the FlagWatcher with the entityclass, it creates all the data it needs to prevent conflicts when sending the
     * datawatcher.
     */
    private void setupWatcher() {
        HashMap<Integer, Object> disguiseValues = DisguiseValues.getMetaValues(getType());
        HashMap<Integer, Object> entityValues = DisguiseValues.getMetaValues(DisguiseType.getType(getEntity().getType()));
        // Start from 2 as they ALL share 0 and 1
        for (int dataNo = 0; dataNo <= 31; dataNo++) {
            // STEP 1. Find out if the watcher has set data on it.
            // If the watcher already set a metadata on this
            if (getWatcher().hasValue(dataNo)) {
                // Better check that the value is stable.
                // To check this, I'm going to check if there exists a default value
                // Then I'm going to check if the watcher value is the same as the default value.
                if (disguiseValues.containsKey(dataNo)) {
                    // Now check if they are the same class, or both null.
                    if (disguiseValues.get(dataNo) == null || getWatcher().getValue(dataNo, null) == null) {
                        if (disguiseValues.get(dataNo) == null && getWatcher().getValue(dataNo, null) == null) {
                            // They are both null. Idk what this means really.
                            continue;
                        }
                    } else if (getWatcher().getValue(dataNo, null).getClass() == disguiseValues.get(dataNo).getClass()) {
                        // The classes are the same. The client "shouldn't" crash.
                        continue;
                    }
                }
            }
            // STEP 2. As the watcher has not set data on it, check if I need to set the default data.
            // If neither of them touch it
            if (!entityValues.containsKey(dataNo) && !disguiseValues.containsKey(dataNo))
                continue;
            // If the disguise has this, but not the entity. Then better set it!
            if (!entityValues.containsKey(dataNo) && disguiseValues.containsKey(dataNo)) {
                getWatcher().setBackupValue(dataNo, disguiseValues.get(dataNo));
                continue;
            }
            // Else if the disguise doesn't have it. But the entity does. Better remove it!
            if (entityValues.containsKey(dataNo) && !disguiseValues.containsKey(dataNo)) {
                getWatcher().setBackupValue(dataNo, null);
                continue;
            }
            Object eObj = entityValues.get(dataNo);
            Object dObj = disguiseValues.get(dataNo);
            if (eObj == null || dObj == null) {
                if (eObj == null && dObj == null)
                    continue;
                else {
                    getWatcher().setBackupValue(dataNo, dObj);
                    continue;
                }
            }
            if (eObj.getClass() != dObj.getClass()) {
                getWatcher().setBackupValue(dataNo, dObj);
                continue;
            }

            // Since they both share it. With the same classes. Time to check if its from something they extend.
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
                baseClass = ReflectionManager.getNmsClass("EntityLiving");
                break;
            case 10:
            case 11:
                baseClass = ReflectionManager.getNmsClass("EntityInsentient");
                break;
            case 16:
                baseClass = ReflectionManager.getNmsClass("EntityAgeable");
                break;
            default:
                break;
            }
            Class nmsEntityClass = ReflectionManager.getNmsEntity(getEntity()).getClass();
            Class nmsDisguiseClass = DisguiseValues.getNmsEntityClass(getType());
            // If they both extend the same base class. They OBVIOUSLY share the same datavalue. Right..?
            if (baseClass != null && baseClass.isAssignableFrom(nmsDisguiseClass) && baseClass.isAssignableFrom(nmsEntityClass))
                continue;

            // So they don't extend a basic class.
            // Maybe if I check that they extend each other..
            // Seeing as I only store the finished forms of entitys. This should raise no problems and allow for more shared
            // datawatchers.
            if (nmsEntityClass.isAssignableFrom(nmsDisguiseClass) || nmsDisguiseClass.isAssignableFrom(nmsEntityClass))
                continue;
            // Well I can't find a reason I should leave it alone. They will probably conflict.
            // Time to set the value to the disguises value so no conflicts!
            getWatcher().setBackupValue(dataNo, disguiseValues.get(dataNo));
        }
    }

    public void setVelocitySent(boolean sendVelocity) {
        this.velocitySent = sendVelocity;
    }

    /**
     * Can the disguised view himself as the disguise
     */
    public void setViewSelfDisguise(boolean viewSelfDisguise) {
        if (isSelfDisguiseVisible() != viewSelfDisguise) {
            this.viewSelfDisguise = viewSelfDisguise;
            if (getEntity() != null && getEntity() instanceof Player) {
                if (DisguiseAPI.getDisguise((Player) getEntity(), getEntity()) == this) {
                    if (isSelfDisguiseVisible()) {
                        DisguiseUtilities.setupFakeDisguise(this);
                    } else
                        DisguiseUtilities.removeSelfDisguise((Player) getEntity());
                }
            }
        }
    }

    public void setWatcher(FlagWatcher newWatcher) {
        watcher = newWatcher;
    }

    @Deprecated
    public boolean viewSelfDisguise() {
        return viewSelfDisguise;
    }
}