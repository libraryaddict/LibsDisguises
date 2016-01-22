package me.libraryaddict.disguise;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise.TargetType;
import me.libraryaddict.disguise.disguisetypes.watchers.HorseWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.ReflectionManager;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class DisguiseAPI {

    public static Disguise constructDisguise(Entity entity) {
        return constructDisguise(entity, true, true, true);
    }

    public static Disguise constructDisguise(Entity entity, boolean doEquipment, boolean doSneak, boolean doSprint) {
        DisguiseType disguiseType = DisguiseType.getType(entity);
        Disguise disguise;
        if (disguiseType.isMisc()) {
            disguise = new MiscDisguise(disguiseType);
        } else if (disguiseType.isMob()) {
            disguise = new MobDisguise(disguiseType);
        } else {
            disguise = new PlayerDisguise(entity.getName());
        }
        FlagWatcher watcher = disguise.getWatcher();
        if (entity instanceof LivingEntity) {
            for (PotionEffect effect : ((LivingEntity) entity).getActivePotionEffects()) {
                ((LivingWatcher) watcher).addPotionEffect(effect.getType());
                if (effect.getType().getName().equals("INVISIBILITY")) {
                    watcher.setInvisible(true);
                }
            }
        }
        if (entity.getFireTicks() > 0) {
            watcher.setBurning(true);
        }
        if (doEquipment && entity instanceof LivingEntity) {
            EntityEquipment equip = ((LivingEntity) entity).getEquipment();
            watcher.setArmor(equip.getArmorContents());
            watcher.setItemInHand(equip.getItemInHand());
            if (disguiseType.getEntityType() == EntityType.HORSE) {
                Horse horse = (Horse) entity;
                HorseInventory horseInventory = horse.getInventory();
                ItemStack saddle = horseInventory.getSaddle();
                if (saddle != null && saddle.getType() == Material.SADDLE) {
                    ((HorseWatcher) watcher).setSaddled(true);
                }
                ((HorseWatcher) watcher).setHorseArmor(horseInventory.getArmor());
            }
        }
        for (Method method : entity.getClass().getMethods()) {
            if ((doSneak || !method.getName().equals("setSneaking")) && (doSprint || !method.getName().equals("setSprinting"))
                    && method.getParameterTypes().length == 0 && method.getReturnType() != void.class) {
                Class methodReturn = method.getReturnType();
                if (methodReturn == float.class || methodReturn == Float.class || methodReturn == Double.class) {
                    methodReturn = double.class;
                }
                int firstCapitalMethod = firstCapital(method.getName());
                if (firstCapitalMethod > 0) {
                    for (Method watcherMethod : watcher.getClass().getMethods()) {
                        if (!watcherMethod.getName().startsWith("get") && watcherMethod.getReturnType() == void.class
                                && watcherMethod.getParameterTypes().length == 1) {
                            int firstCapitalWatcher = firstCapital(watcherMethod.getName());
                            if (firstCapitalWatcher > 0
                                    && method.getName().substring(firstCapitalMethod)
                                    .equalsIgnoreCase(watcherMethod.getName().substring(firstCapitalWatcher))) {
                                Class methodParam = watcherMethod.getParameterTypes()[0];
                                if (methodParam == float.class || methodParam == Float.class || methodParam == Double.class) {
                                    methodParam = double.class;
                                } else if (methodParam == AnimalColor.class) {
                                    methodParam = DyeColor.class;
                                }
                                if (methodReturn == methodParam) {
                                    try {
                                        Object value = method.invoke(entity);
                                        if (value != null) {
                                            Class toCast = watcherMethod.getParameterTypes()[0];
                                            if (!(toCast.isInstance(value))) {
                                                if (toCast == float.class) {
                                                    if (value instanceof Float) {
                                                        value = value;
                                                    } else {
                                                        double d = (Double) value;
                                                        value = (float) d;
                                                    }
                                                } else if (toCast == double.class) {
                                                    if (value instanceof Double) {
                                                        value = value;
                                                    } else {
                                                        float d = (Float) value;
                                                        value = (double) d;
                                                    }
                                                } else if (toCast == AnimalColor.class) {
                                                    value = AnimalColor.valueOf(((DyeColor) value).name());
                                                }
                                            }
                                            if (value instanceof Boolean && !(Boolean) value
                                                    && watcherMethod.getDeclaringClass() == FlagWatcher.class) {
                                                continue;
                                            }
                                        }
                                        watcherMethod.invoke(watcher, value);
                                    } catch (Exception ex) {
                                        ex.printStackTrace(System.out);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return disguise;
    }

    public static void disguiseEntity(Entity entity, Disguise disguise) {
        // If they are trying to disguise a null entity or use a null disguise
        // Just return.
        if (entity == null || disguise == null) {
            return;
        }
        // The event wasn't cancelled.
        // If the disguise entity isn't the same as the one we are disguising
        if (disguise.getEntity() != entity) {
            // If the disguise entity actually exists
            if (disguise.getEntity() != null) {
                // Clone the disguise
                disguise = disguise.clone();
            }
            // Set the disguise's entity
            disguise.setEntity(entity);
        }
        if (Disguise.getViewSelf().contains(disguise.getEntity().getUniqueId())) {
            disguise.setViewSelfDisguise(true);
        }
        disguise.startDisguise();
    }

    public static void disguiseIgnorePlayers(Entity entity, Disguise disguise, Collection playersToNotSeeDisguise) {
        if (disguise.getEntity() != null) {
            disguise = disguise.clone();
        }
        ((TargetedDisguise) disguise).setDisguiseTarget(TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS);
        for (Object obj : playersToNotSeeDisguise) {
            if (obj instanceof String) {
                ((TargetedDisguise) disguise).addPlayer((String) obj);
            } else if (obj instanceof Player) {
                ((TargetedDisguise) disguise).addPlayer(((Player) obj).getName());
            }
        }
        disguiseEntity(entity, disguise);
    }

    @Deprecated
    public static void disguiseIgnorePlayers(Entity entity, Disguise disguise, List<String> playersToNotSeeDisguise) {
        disguiseIgnorePlayers(entity, disguise, (Collection) playersToNotSeeDisguise);
    }

    public static void disguiseIgnorePlayers(Entity entity, Disguise disguise, Player... playersToNotSeeDisguise) {
        disguiseIgnorePlayers(entity, disguise, (Collection) Arrays.asList(playersToNotSeeDisguise));
    }

    public static void disguiseIgnorePlayers(Entity entity, Disguise disguise, String... playersToNotSeeDisguise) {
        disguiseIgnorePlayers(entity, disguise, (Collection) Arrays.asList(playersToNotSeeDisguise));
    }

    /**
     * Disguise the next entity to spawn with this disguise. This may not work however if the entity doesn't actually spawn.
     *
     * @param disguise
     * @return
     */
    public static int disguiseNextEntity(Disguise disguise) {
        if (disguise == null) {
            return -1;
        }
        if (disguise.getEntity() != null || DisguiseUtilities.getDisguises().containsValue(disguise)) {
            disguise = disguise.clone();
        }
        try {
            Field field = ReflectionManager.getNmsClass("Entity").getDeclaredField("entityCount");
            field.setAccessible(true);
            int id = field.getInt(null);
            DisguiseUtilities.addFutureDisguise(id, (TargetedDisguise) disguise);
            return id;
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace(System.out);
        }
        return -1;
    }

    /**
     * Disguise this entity with this disguise
     *
     * @param entity
     * @param disguise
     */
    public static void disguiseToAll(Entity entity, Disguise disguise) {
        if (disguise.getEntity() != null) {
            disguise = disguise.clone();
        }
        // You called the disguiseToAll method foolish mortal! Prepare to have your custom settings wiped!!!
        ((TargetedDisguise) disguise).setDisguiseTarget(TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS);
        for (String observer : ((TargetedDisguise) disguise).getObservers()) {
            ((TargetedDisguise) disguise).removePlayer(observer);
        }
        disguiseEntity(entity, disguise);
    }

    public static void disguiseToPlayers(Entity entity, Disguise disguise, Collection playersToViewDisguise) {
        if (disguise.getEntity() != null) {
            disguise = disguise.clone();
        }
        ((TargetedDisguise) disguise).setDisguiseTarget(TargetType.HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS);
        for (Object obj : playersToViewDisguise) {
            if (obj instanceof String) {
                ((TargetedDisguise) disguise).addPlayer((String) obj);
            } else if (obj instanceof Player) {
                ((TargetedDisguise) disguise).addPlayer(((Player) obj).getName());
            }
        }
        disguiseEntity(entity, disguise);
    }

    @Deprecated
    public static void disguiseToPlayers(Entity entity, Disguise disguise, List<String> playersToViewDisguise) {
        disguiseToPlayers(entity, disguise, (Collection) playersToViewDisguise);
    }

    public static void disguiseToPlayers(Entity entity, Disguise disguise, Player... playersToViewDisguise) {
        disguiseToPlayers(entity, disguise, (Collection) Arrays.asList(playersToViewDisguise));
    }

    public static void disguiseToPlayers(Entity entity, Disguise disguise, String... playersToViewDisguise) {
        disguiseToPlayers(entity, disguise, (Collection) Arrays.asList(playersToViewDisguise));
    }

    private static int firstCapital(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isUpperCase(str.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the disguise of a entity
     *
     * @param disguised
     * @return
     */
    public static Disguise getDisguise(Entity disguised) {
        if (disguised == null) {
            return null;
        }
        return DisguiseUtilities.getMainDisguise(disguised.getUniqueId());
    }

    /**
     * Get the disguise of a entity
     *
     * @param observer
     * @param disguised
     * @return
     */
    public static Disguise getDisguise(Player observer, Entity disguised) {
        if (disguised == null || observer == null) {
            return null;
        }
        return DisguiseUtilities.getDisguise(observer, disguised);
    }

    /**
     * Get the disguises of a entity
     *
     * @param disguised
     * @return
     */
    public static Disguise[] getDisguises(Entity disguised) {
        if (disguised == null) {
            return null;
        }
        return DisguiseUtilities.getDisguises(disguised.getUniqueId());
    }

    /**
     * Get the ID of a fake disguise for a entityplayer
     *
     * @param entityId
     * @return
     */
    @Deprecated
    public static int getFakeDisguise(UUID entityId) {
        return -10;
    }

    public static int getSelfDisguiseId() {
        return -10;
    }

    /**
     * Is this entity disguised
     *
     * @param disguised
     * @return
     */
    public static boolean isDisguised(Entity disguised) {
        return getDisguise(disguised) != null;
    }

    /**
     * Is this entity disguised
     *
     * @param observer
     * @param disguised
     * @return
     */
    public static boolean isDisguised(Player observer, Entity disguised) {
        return getDisguise(observer, disguised) != null;
    }

    public static boolean isDisguiseInUse(Disguise disguise) {
        return disguise.isDisguiseInUse();
    }

    public static boolean isSelfDisguised(Player player) {
        return DisguiseUtilities.getSelfDisguised().contains(player.getUniqueId());
    }

    /**
     * Returns true if the entitiy has /disguiseviewself toggled on.
     *
     * @param entity
     * @return
     */
    public static boolean isViewSelfToggled(Entity entity) {
        return isDisguised(entity) ? getDisguise(entity).isSelfDisguiseVisible() : Disguise.getViewSelf().contains(entity.getUniqueId());
    }

    /**
     * Undisguise the entity. This doesn't let you cancel the UndisguiseEvent if the entity is no longer valid. Aka removed from the world.
     *
     * @param entity
     */
    public static void undisguiseToAll(Entity entity) {
        Disguise[] disguises = getDisguises(entity);
        for (Disguise disguise : disguises) {
            disguise.removeDisguise();
        }
    }

    /**
     * Set whether this player can see his own disguise or not.
     *
     * @param entity
     * @param toggled
     */
    public static void setViewDisguiseToggled(Entity entity, boolean toggled) {
        if (isDisguised(entity)) {
            Disguise disguise = getDisguise(entity);
            disguise.setViewSelfDisguise(toggled);
        }
        if (toggled) {
            if (!Disguise.getViewSelf().contains(entity.getUniqueId())) {
                Disguise.getViewSelf().add(entity.getUniqueId());
            }
        } else {
            Disguise.getViewSelf().remove(entity.getUniqueId());
        }
    }

    private DisguiseAPI() {
    }
}
