package me.libraryaddict.disguise;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import lombok.Getter;
import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise.TargetType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DisguiseAPI {
    private static int selfDisguiseId = ReflectionManager.getNewEntityId(true);
    @Getter
    private static int entityAttachmentId = ReflectionManager.getNewEntityId(true);

    public static void addCustomDisguise(String disguiseName, String disguiseInfo) throws DisguiseParseException {
        // Dirty fix for anyone that somehow got this far with a . in the name, invalid yaml!
        disguiseName = disguiseName.replace(".", "");

        try {
            DisguiseConfig.removeCustomDisguise(disguiseName);
            DisguiseConfig.addCustomDisguise(disguiseName, disguiseInfo);

            File disguisesFile = new File(LibsDisguises.getInstance().getDataFolder(), "configs/disguises.yml");

            if (!disguisesFile.exists()) {
                disguisesFile.getParentFile().mkdirs();
                disguisesFile.createNewFile();
            }

            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(disguisesFile);

            if (!configuration.isConfigurationSection("Disguises")) {
                configuration.createSection("Disguises");
            }

            ConfigurationSection section = configuration.getConfigurationSection("Disguises");
            section.set(disguiseName, disguiseInfo.replace("\n", "\\n").replace("\r", "\\r"));

            configuration.save(disguisesFile);

            DisguiseUtilities.getLogger().info("Added new Custom Disguise " + disguiseName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addGameProfile(String profileName, WrappedGameProfile gameProfile) {
        DisguiseUtilities.addGameProfile(profileName, gameProfile);
    }

    public static String getRawCustomDisguise(String disguiseName) {
        Map.Entry<DisguisePerm, String> entry = DisguiseConfig.getRawCustomDisguise(disguiseName);

        if (entry == null) {
            return null;
        }

        return entry.getValue();
    }

    public static Disguise getCustomDisguise(String disguiseName) {
        Map.Entry<DisguisePerm, Disguise> disguise = DisguiseConfig.getCustomDisguise(disguiseName);

        if (disguise == null) {
            return null;
        }

        return disguise.getValue();
    }

    public static Disguise constructDisguise(Entity entity) {
        return constructDisguise(entity, true, false);
    }

    public static Disguise constructDisguise(Entity entity, boolean doEquipment, boolean displayExtraAnimations) {
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

        if (doEquipment && entity instanceof LivingEntity) {
            EntityEquipment equip = ((LivingEntity) entity).getEquipment();

            watcher.setArmor(equip.getArmorContents());

            ItemStack mainItem = equip.getItemInMainHand();

            if (mainItem != null && mainItem.getType() != Material.AIR) {
                watcher.setItemInMainHand(mainItem);
            }

            ItemStack offItem = equip.getItemInMainHand();

            if (offItem != null && offItem.getType() != Material.AIR) {
                watcher.setItemInOffHand(offItem);
            }
        }

        WrappedDataWatcher dataWatcher = WrappedDataWatcher.getEntityWatcher(entity);

        for (WrappedWatchableObject obj : dataWatcher.getWatchableObjects()) {
            MetaIndex index = MetaIndex.getMetaIndex(watcher.getClass(), obj.getIndex());

            if (index == null) {
                continue;
            }

            if (index.getDefault() == obj.getValue() || index.getDefault() == obj.getRawValue()) {
                continue;
            }

                watcher.setUnsafeData(index, obj.getRawValue());

            // Update the meta for 0, otherwise boolean be weird
            if (index == MetaIndex.ENTITY_META) {
                watcher.setSprinting(watcher.isSprinting() && displayExtraAnimations);
                watcher.setFlyingWithElytra(watcher.isFlyingWithElytra() && displayExtraAnimations);
                watcher.setRightClicking(watcher.isRightClicking() && displayExtraAnimations);
                watcher.setSneaking(watcher.isSneaking() && displayExtraAnimations);
                watcher.setSwimming(watcher.isSwimming() && displayExtraAnimations);

                if (!displayExtraAnimations) {
                    Arrays.fill(watcher.getModifiedEntityAnimations(), false);
                }

                watcher.setGlowing(watcher.isGlowing());
                watcher.setInvisible(watcher.isInvisible());
            }
        }

        return disguise;
    }

    public static void disguiseEntity(Entity entity, Disguise disguise) {
        disguiseEntity(null, entity, disguise);
    }

    public static void disguiseEntity(CommandSender commandSender, Entity entity, Disguise disguise) {
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

        // They prefer to have the opposite of whatever the view disguises option is
        if (hasSelfDisguisePreference(entity) && disguise.isSelfDisguiseVisible() == DisguiseConfig.isViewSelfDisguisesDefault()) {
            disguise.setViewSelfDisguise(!disguise.isSelfDisguiseVisible());
        }

        if (hasActionBarPreference(entity) && !isActionBarShown(entity)) {
            disguise.setNotifyBar(DisguiseConfig.NotifyBar.NONE);
        }

        disguise.startDisguise(commandSender);
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
        disguiseIgnorePlayers(entity, disguise, Arrays.asList(playersToNotSeeDisguise));
    }

    public static void disguiseIgnorePlayers(Entity entity, Disguise disguise, String... playersToNotSeeDisguise) {
        disguiseIgnorePlayers(entity, disguise, (Collection) Arrays.asList(playersToNotSeeDisguise));
    }

    /**
     * Disguise the next entity to spawn, this means you need to spawn an entity immediately after calling this.
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

        int id = ReflectionManager.getNewEntityId(false);
        DisguiseUtilities.addFutureDisguise(id, (TargetedDisguise) disguise);

        return id;
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
        disguiseToPlayers(entity, disguise, Arrays.asList(playersToViewDisguise));
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

        return DisguiseUtilities.getMainDisguise(disguised.getEntityId());
    }

    public static String parseToString(Disguise disguise, boolean outputSkin) {
        return DisguiseParser.parseToString(disguise, outputSkin);
    }

    public static String parseToString(Disguise disguise) {
        return parseToString(disguise, true);
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

        return DisguiseUtilities.getDisguises(disguised.getEntityId());
    }

    public static int getSelfDisguiseId() {
        return selfDisguiseId;
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
        return hasSelfDisguisePreference(entity) != DisguiseConfig.isViewSelfDisguisesDefault();
    }

    /**
     * Returns true if the entitiy has /disguiseviewself toggled on.
     *
     * @param entity
     * @return
     */
    public static boolean isActionBarShown(Entity entity) {
        return !hasActionBarPreference(entity);
    }

    public static boolean hasSelfDisguisePreference(Entity entity) {
        return DisguiseUtilities.getViewSelf().contains(entity.getUniqueId());
    }

    public static boolean hasActionBarPreference(Entity entity) {
        return DisguiseUtilities.getViewBar().contains(entity.getUniqueId());
    }

    /**
     * Undisguise the entity. This doesn't let you cancel the UndisguiseEvent if the entity is no longer valid. Aka
     * removed from
     * the world.
     *
     * @param entity
     */
    public static void undisguiseToAll(Entity entity) {
        undisguiseToAll(null, entity);
    }

    /**
     * Undisguise the entity. This doesn't let you cancel the UndisguiseEvent if the entity is no longer valid. Aka
     * removed from
     * the world.
     *
     * @param entity
     */
    public static void undisguiseToAll(CommandSender sender, Entity entity) {
        Disguise[] disguises = getDisguises(entity);

        for (Disguise disguise : disguises) {
            disguise.removeDisguise(sender);
        }
    }

    /**
     * Set whether this player can see his own disguise or not.
     *
     * @param entity
     * @param canSeeSelfDisguises
     */
    public static void setViewDisguiseToggled(Entity entity, boolean canSeeSelfDisguises) {
        if (isDisguised(entity)) {
            Disguise[] disguises = getDisguises(entity);

            for (Disguise disguise : disguises) {
                disguise.setViewSelfDisguise(canSeeSelfDisguises);
            }
        }

        if (!canSeeSelfDisguises == DisguiseConfig.isViewSelfDisguisesDefault()) {
            if (!hasSelfDisguisePreference(entity)) {
                DisguiseUtilities.getViewSelf().add(entity.getUniqueId());
                DisguiseUtilities.addSaveAttempt();
            }
        } else {
            DisguiseUtilities.getViewSelf().remove(entity.getUniqueId());
            DisguiseUtilities.addSaveAttempt();
        }
    }

    public static void setActionBarShown(Player player, boolean isShown) {
        if (isDisguised(player)) {
            Disguise[] disguises = getDisguises(player);

            for (Disguise disguise : disguises) {
                disguise.setNotifyBar(isShown ? DisguiseConfig.getNotifyBar() : DisguiseConfig.NotifyBar.NONE);
            }
        }

        // If default is view and we want the opposite
        if (!isShown) {
            if (!hasActionBarPreference(player)) {
                DisguiseUtilities.getViewBar().add(player.getUniqueId());
                DisguiseUtilities.addSaveAttempt();
            }
        } else {
            DisguiseUtilities.getViewBar().remove(player.getUniqueId());
            DisguiseUtilities.addSaveAttempt();
        }
    }

    private DisguiseAPI() {
    }
}
