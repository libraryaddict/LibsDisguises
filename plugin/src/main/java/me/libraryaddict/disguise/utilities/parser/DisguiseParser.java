package me.libraryaddict.disguise.utilities.parser;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.ModdedDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.FallingBlockWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.modded.ModdedEntity;
import me.libraryaddict.disguise.utilities.modded.ModdedManager;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import me.libraryaddict.disguise.utilities.watchers.DisguiseMethods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class DisguiseParser {
    /**
     * <Setter, <Getter, DefaultValue>>
     */
    private static HashMap<WatcherMethod, Map.Entry<WatcherMethod, Object>> defaultWatcherValues = new HashMap<>();

    public static void createDefaultMethods() {
        try {
            ArrayList<WatcherMethod> allMethods = ParamInfoManager.getDisguiseMethods().getMethods();

            for (DisguiseType type : DisguiseType.values()) {
                if (type.getEntityType() == null) {
                    continue;
                }

                Disguise disguise;

                if (type.isMisc()) {
                    disguise = new MiscDisguise(type);
                } else if (type.isMob()) {
                    disguise = new MobDisguise(type);
                } else if (type.isPlayer()) {
                    disguise = new PlayerDisguise("Foobar");
                } else {
                    continue;
                }

                FlagWatcher watcher = type.getWatcherClass().getConstructor(Disguise.class).newInstance(disguise);

                WatcherMethod[] methods = ParamInfoManager.getDisguiseWatcherMethods(watcher.getClass(), true);

                for (WatcherMethod setMethod : methods) {
                    // Invalidate methods that can't be handled normally
                    if (setMethod.getName().equals("addPotionEffect")) {
                        continue;
                    } else if (setMethod.getName().equals("setSkin") && setMethod.getParam() == String.class) {
                        continue;
                    } else if (setMethod.getName().equals("setTarget") && setMethod.getParam() != int.class) {
                        continue;
                    } else if ((setMethod.getName().equals("setCustomName") || setMethod.getName().equals("setCustomNameVisible")) &&
                        disguise.isPlayerDisguise()) {
                        // Player Disguise overrides the behavior of custom name, so we definitely don't want it judged on a global scale
                        continue;
                    } else if (setMethod.getName().equals("setItemInMainHand") && setMethod.getParam() == Material.class) {
                        continue;
                    } else if (setMethod.getName().matches("setArmor") && setMethod.getParam() == ItemStack[].class) {
                        continue;
                    }

                    String getName = setMethod.getName().substring(3); // Remove 'set'

                    if (getName.equals("HasNectar")) {
                        getName = "hasNectar";
                    } else if (getName.equals("HasStung")) {
                        getName = "hasStung";
                    } else if (getName.matches("^Has((Left)|(Right))Horn$")) {
                        getName = "has" + getName.substring(3);
                    } else if (setMethod.getParam().isAssignableFrom(boolean.class)) {
                        getName = "is" + getName;
                    } else {
                        getName = "get" + getName;
                    }

                    WatcherMethod getMethod = null;

                    for (WatcherMethod m : allMethods) {
                        if (m.getWatcherClass() != setMethod.getWatcherClass() || m.getParam() != null) {
                            continue;
                        }

                        if (!m.getName().equals(getName)) {
                            continue;
                        }

                        getMethod = m;
                        break;
                    }

                    if (getMethod == null) {
                        DisguiseUtilities.getLogger().severe(
                            String.format("No such method '%s' when looking for the companion of '%s' in '%s'", getName, setMethod.getName(),
                                setMethod.getWatcherClass().getSimpleName()));
                        continue;
                    } else if (getMethod.getReturnType() != setMethod.getParam()) {
                        DisguiseUtilities.getLogger().severe(
                            String.format("Invalid return type of '%s' when looking for the companion of '%s' in '%s'", getName, setMethod.getName(),
                                setMethod.getWatcherClass().getSimpleName()));
                        continue;
                    }

                    Object defaultValue = null;

                    // Value is randomish so shouldn't be checked, should always specify value when setting
                    if (!setMethod.isRandomDefault()) {
                        Object invokeWith = watcher;

                        if (!FlagWatcher.class.isAssignableFrom(getMethod.getWatcherClass())) {
                            invokeWith = disguise;
                        }

                        defaultValue = getMethod.getMethod().bindTo(invokeWith).invoke();
                    }

                    addWatcherDefault(setMethod, getMethod, defaultValue);
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static HashMap<WatcherMethod, Entry<WatcherMethod, Object>> getMethodDefaults() {
        return defaultWatcherValues;
    }

    public static String parseToString(Disguise disguise) {
        return parseToString(disguise, true);
    }

    public static String parseToString(Disguise disguise, boolean outputSkinData) {
        return parseToString(disguise, outputSkinData, false);
    }

    /**
     * Not outputting skin information is not garanteed to display the correct player name
     */
    public static String parseToString(Disguise disguise, boolean outputSkinData, boolean includeCustomData) {
        try {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(disguise.getType().toReadable().replace(" ", "_"));

            if (disguise.isPlayerDisguise()) {
                stringBuilder.append(" ").append(DisguiseUtilities.quote(((PlayerDisguise) disguise).getName()));
            }

            WatcherMethod[] methods = ParamInfoManager.getDisguiseWatcherMethods(disguise.getType().getWatcherClass());

            for (int i = methods.length - 1; i >= 0; i--) {
                WatcherMethod m = methods[i];

                // Special handling for this method
                if (m.getName().equals("addPotionEffect")) {
                    MethodHandle getPotion =
                        MethodHandles.publicLookup().bind(disguise.getWatcher(), "getPotionEffects", MethodType.methodType(PotionEffectType[].class));
                    PotionEffectType[] types = (PotionEffectType[]) getPotion.invoke();

                    for (PotionEffectType type : types) {
                        if (type == null) {
                            continue;
                        }

                        stringBuilder.append(" ").append(TranslateType.DISGUISE_OPTIONS.get(m.getName())).append(" ")
                            .append(TranslateType.DISGUISE_OPTIONS_PARAMETERS.get(type.getName()));
                    }

                    continue;
                }

                // Also for this method. You can't override it, so why output it
                if (m.getName().equals("setNoGravity")) {
                    continue;
                }

                Entry<WatcherMethod, Object> entry = defaultWatcherValues.get(m);

                if (entry == null) {
                    continue;
                }

                Object invokeWith = m.getWatcherClass().isInstance(disguise) ? disguise : disguise.getWatcher();

                Object ourValue = entry.getKey().getMethod().bindTo(invokeWith).invoke();

                // Escape a hacky fix for custom names, disguised players with custom names don't want to show it
                // so it was set to an empty string.
                if ("".equals(ourValue) && m.getName().equals("setCustomName")) {
                    ourValue = null;
                }

                if (m.getName().equals("setSkin") && !outputSkinData) {
                    PlayerDisguise pDisg = (PlayerDisguise) disguise;
                    ourValue = pDisg.getName();

                    if (pDisg.getSkin() != null) {
                        ourValue = pDisg.getSkin();
                    } else if (pDisg.getGameProfile() != null && pDisg.getGameProfile().getName() != null) {
                        ourValue = pDisg.getGameProfile().getName();
                    }

                    if (ourValue.equals(pDisg.getName())) {
                        continue;
                    }
                } else {
                    // If its the same as default, continue
                    if (!m.isRandomDefault() && Objects.deepEquals(entry.getValue(), ourValue)) {
                        continue;
                    }
                }

                stringBuilder.append(" ").append(TranslateType.DISGUISE_OPTIONS.get(m.getName()));

                if (ourValue instanceof Boolean && (Boolean) ourValue) {
                    continue;
                }

                String valueString;

                if (ourValue != null) {
                    valueString = ParamInfoManager.getParamInfo(ourValue.getClass()).toString(ourValue);

                    if (ourValue instanceof String) {
                        valueString = TranslateType.DISGUISE_OPTIONS_PARAMETERS.get(valueString);
                    }

                    valueString = DisguiseUtilities.quote(valueString);
                } else {
                    valueString = TranslateType.DISGUISE_OPTIONS_PARAMETERS.get("null");
                }

                stringBuilder.append(" ").append(valueString);
            }

            if (includeCustomData) {
                HashMap<String, Object> meta = disguise.getCustomData();
                LinkedHashMap<String, String> serializedMeta = new LinkedHashMap<>();

                for (Entry<String, Object> entry : meta.entrySet()) {
                    Object val = entry.getValue();

                    try {
                        if (val == null) {
                            serializedMeta.put(entry.getKey(), "null");
                            continue;
                        }

                        String serialized = DisguiseUtilities.getGson().toJson(val);

                        serializedMeta.put(entry.getKey(), val.getClass().getName() + ":" + serialized);
                    } catch (Throwable throwable) {
                        DisguiseUtilities.getLogger()
                            .warning("Unable to properly serialize the metadata on a disguise, the metadata was saved under name '" + entry.getKey() + "'");

                        if (!(throwable instanceof StackOverflowError)) {
                            throwable.printStackTrace();
                        }
                        continue;
                    }
                }

                if (!serializedMeta.isEmpty()) {
                    String serialized = DisguiseUtilities.getGson().toJson(serializedMeta);

                    stringBuilder.append(" ").append("setCustomData").append(" ").append(DisguiseUtilities.quote(serialized));
                }
            }

            return stringBuilder.toString();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void addWatcherDefault(WatcherMethod setMethod, WatcherMethod getMethod, Object object) {
        if (defaultWatcherValues.containsKey(setMethod)) {
            Object dObj = defaultWatcherValues.get(setMethod).getValue();

            if (!Objects.deepEquals(dObj, object)) {
                throw new IllegalStateException(String.format(
                    "%s has conflicting values in class %s! This means it expected the same value again but " + "received a " +
                        "different value on a different disguise! %s is not the same as %s!", setMethod.toString(), setMethod.toString(), object, dObj));
            }

            return;
        }

        Map.Entry<WatcherMethod, Object> entry = new HashMap.SimpleEntry<>(getMethod, object);

        defaultWatcherValues.put(setMethod, entry);
    }

    private static void doCheck(CommandSender sender, DisguisePermissions permissions, DisguisePerm disguisePerm, Collection<String> usedOptions)
        throws DisguiseParseException {

        if (!permissions.isAllowedDisguise(disguisePerm, usedOptions)) {
            throw new DisguiseParseException(LibsMsg.D_PARSE_NOPERM, usedOptions.stream().reduce((first, second) -> second).orElse(null));
        }
    }

    public static DisguisePerm getDisguisePerm(String name) {
        for (DisguisePerm perm : getDisguisePerms()) {
            if (!perm.toReadable().replaceAll("[ |_]", "").equalsIgnoreCase(name.replaceAll("[ |_]", ""))) {
                continue;
            }

            return perm;
        }

        if (name.equalsIgnoreCase("p")) {
            return getDisguisePerm(DisguiseType.PLAYER.toReadable());
        }

        return null;
    }

    public static DisguisePerm[] getDisguisePerms() {
        ArrayList<DisguisePerm> perms = new ArrayList<>();

        for (DisguiseType disguiseType : DisguiseType.values()) {
            if (disguiseType.getEntityType() == null || disguiseType.isCustom()) {
                continue;
            }

            perms.add(new DisguisePerm(disguiseType));
        }

        for (Entry<DisguisePerm, String> entry : DisguiseConfig.getCustomDisguises().entrySet()) {
            perms.add(entry.getKey());
        }

        perms.addAll(ModdedManager.getDisguiseTypes());

        return perms.toArray(new DisguisePerm[0]);
    }

    /**
     * Get perms for the node. Returns a hashmap of allowed disguisetypes and their options
     */
    public static DisguisePermissions getPermissions(CommandSender sender, String commandName) {
        return new DisguisePermissions(sender, commandName);
    }

    private static boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static String getName(CommandSender entity) {
        if (entity == null) {
            return "??";
        }

        if (entity instanceof Player) {
            return entity.getName();
        }

        if (entity instanceof Entity) {
            if (((Entity) entity).getCustomName() != null && ((Entity) entity).getCustomName().length() > 0) {
                return ((Entity) entity).getCustomName();
            }
        }

        return entity.getName();
    }

    private static String getSkin(CommandSender entity) {
        if (entity == null) {
            return "??";
        }

        if (entity instanceof Player) {
            WrappedGameProfile gameProfile = ReflectionManager.getGameProfile((Player) entity);

            if (gameProfile != null) {
                return DisguiseUtilities.getGson().toJson(gameProfile);
            }
        }

        return "{}";
    }

    public static String[] parsePlaceholders(String[] args, CommandSender user, CommandSender target) {
        return parsePlaceholders(args, getName(user), DisguiseUtilities.getDisplayName(user), getSkin(user), getName(target),
            DisguiseUtilities.getDisplayName(target), DisguiseParser.getSkin(target), getEntityEquipment(user), getEntityEquipment(target));
    }

    private static EntityEquipment getEntityEquipment(CommandSender entity) {
        return entity instanceof LivingEntity ? ((LivingEntity) entity).getEquipment() : null;
    }

    public static String[] parsePlaceholders(String[] args, String userName, String userSkin, String targetName, String targetSkin, EntityEquipment equip,
                                             EntityEquipment targetEquip) {
        return parsePlaceholders(args, userName, userName, userSkin, targetName, targetName, targetSkin, equip, targetEquip);
    }

    public static String[] parsePlaceholders(String[] args, String userName, String userDisplayname, String userSkin, String targetName,
                                             String targetDisplayname, String targetSkin, EntityEquipment equip, EntityEquipment targetEquip) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            arg = replace(arg, "%name%", userName);
            arg = replace(arg, "%displayname%", userDisplayname);
            arg = replace(arg, "%skin%", userSkin);
            arg = replace(arg, "%held-item%", equip == null ? null : equip.getItemInMainHand());
            arg = replace(arg, "%offhand-item%", equip == null ? null : equip.getItemInOffHand());
            arg = replace(arg, "%armor%", equip == null ? null : equip.getArmorContents());
            arg = replace(arg, "%helmet%", equip == null ? null : equip.getHelmet());
            arg = replace(arg, "%chestplate%", equip == null ? null : equip.getChestplate());
            arg = replace(arg, "%leggings%", equip == null ? null : equip.getLeggings());
            arg = replace(arg, "%boots%", equip == null ? null : equip.getBoots());

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!arg.contains("%" + p.getName() + "-")) {
                    continue;
                }

                String name = p.getName();

                arg = replace(arg, "%" + name + "-name%", name);
                arg = replace(arg, "%" + name + "-displayname%", DisguiseUtilities.getDisplayName(p));
                arg = replace(arg, "%" + name + "-skin%", getSkin(p));

                EntityEquipment pEquip = p.getEquipment();

                arg = replace(arg, "%" + name + "-held-item%", pEquip == null ? null : pEquip.getItemInMainHand());
                arg = replace(arg, "%" + name + "-offhand-item%", pEquip == null ? null : pEquip.getItemInOffHand());
                arg = replace(arg, "%" + name + "-armor%", pEquip == null ? null : pEquip.getArmorContents());
                arg = replace(arg, "%" + name + "-helmet%", pEquip == null ? null : pEquip.getHelmet());
                arg = replace(arg, "%" + name + "-chestplate%", pEquip == null ? null : pEquip.getChestplate());
                arg = replace(arg, "%" + name + "-leggings%", pEquip == null ? null : pEquip.getLeggings());
                arg = replace(arg, "%" + name + "-boots%", pEquip == null ? null : pEquip.getBoots());
            }

            arg = replace(arg, "%target-name%", targetName);
            arg = replace(arg, "%target-displayname%", targetDisplayname);
            arg = replace(arg, "%target-skin%", targetSkin);
            arg = replace(arg, "%target-held-item%", targetEquip == null ? null : targetEquip.getItemInMainHand());
            arg = replace(arg, "%target-offhand-item%", targetEquip == null ? null : targetEquip.getItemInOffHand());
            arg = replace(arg, "%target-armor%", targetEquip == null ? null : targetEquip.getArmorContents());
            arg = replace(arg, "%target-helmet%", targetEquip == null ? null : targetEquip.getHelmet());
            arg = replace(arg, "%target-chestplate%", targetEquip == null ? null : targetEquip.getChestplate());
            arg = replace(arg, "%target-leggings%", targetEquip == null ? null : targetEquip.getLeggings());
            arg = replace(arg, "%target-boots%", targetEquip == null ? null : targetEquip.getBoots());

            args[i] = arg;
        }

        return args;
    }

    private static String replace(String string, String value, Object toReplace) {
        if (!string.contains(value)) {
            return string;
        }

        String oValue;

        if (toReplace != null) {
            oValue = ParamInfoManager.toString(toReplace);
        } else {
            oValue = "null";
        }

        return string.replace(value, oValue);
    }

    public static long parseStringToTime(String string) throws DisguiseParseException {
        string = string.toLowerCase(Locale.ENGLISH);

        if (!string.matches("([0-9]+[a-z]+)+")) {
            throw new DisguiseParseException(LibsMsg.PARSE_INVALID_TIME_SEQUENCE, string);
        }

        String[] split = string.split("((?<=[a-zA-Z])(?=[0-9]))|((?<=[0-9])(?=[a-zA-Z]))");

        long time = 0;

        for (int i = 0; i < split.length; i += 2) {
            String t = split[i + 1];
            long v = Long.parseLong(split[i]);

            if (t.equals("s") || t.equals("sec") || t.equals("secs") || t.equals("seconds")) {
                time += v;
            } else if (t.equals("m") || t.equals("min") || t.equals("minute") || t.equals("minutes")) {
                time += TimeUnit.MINUTES.toSeconds(v);
            } else if (t.equals("h") || t.equals("hour") || t.equals("hours")) {
                time += TimeUnit.HOURS.toSeconds(v);
            } else if (t.equals("d") || t.equals("day") || t.equals("days")) {
                time += TimeUnit.DAYS.toSeconds(v);
            } else if (t.equals("w") || t.equals("week") || t.equals("weeks")) {
                time += TimeUnit.DAYS.toSeconds(v) * 7;
            } else if (t.equals("mon") || t.equals("month") || t.equals("months")) {
                time += TimeUnit.DAYS.toSeconds(v) * 31;
            } else if (t.equals("y") || t.equals("year") || t.equals("years")) {
                time += TimeUnit.DAYS.toSeconds(v) * 365;
            } else {
                throw new DisguiseParseException(LibsMsg.PARSE_INVALID_TIME, t);
            }
        }

        return time;
    }

    /**
     * Experimentally parses the arguments to test if this is a valid disguise
     *
     * @param sender
     * @param permNode
     * @param args
     * @param permissions
     * @return
     * @throws DisguiseParseException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Disguise parseTestDisguise(CommandSender sender, String permNode, String[] args, DisguisePermissions permissions) throws Throwable {

        // Clone array so original array isn't modified
        args = Arrays.copyOf(args, args.length);

        String skin = "{\"id\":\"a149f81bf7844f8987c554afdd4db533\",\"name\":\"libraryaddict\"," + "\"properties\":[]}";
        // Fill in fake data
        args = parsePlaceholders(args, "libraryaddict", skin, "libraryaddict", skin, null, null);

        // Parse disguise
        return parseDisguise(sender, null, permNode, args, permissions);
    }

    public static void modifyDisguise(Disguise disguise, Entity target, String[] params) throws Throwable {
        if (target != null) {
            params = DisguiseParser.parsePlaceholders(params, target, target);
        }

        DisguiseParser.callMethods(Bukkit.getConsoleSender(), disguise, new DisguisePermissions(Bukkit.getConsoleSender(), "disguise"),
            new DisguisePerm(disguise.getType()), new ArrayList<>(), params, "Disguise");
    }

    public static void modifyDisguise(Disguise disguise, String[] params) throws Throwable {
        modifyDisguise(disguise, null, params);
    }

    public static void modifyDisguise(Disguise disguise, String params) throws Throwable {
        modifyDisguise(disguise, DisguiseUtilities.split(params));
    }

    public static void modifyDisguise(Disguise disguise, Entity target, String params) throws Throwable {
        modifyDisguise(disguise, target, DisguiseUtilities.split(params));
    }

    public static Disguise parseDisguise(String disguise) throws Throwable {
        return parseDisguise(Bukkit.getConsoleSender(), null, disguise);
    }

    public static Disguise parseDisguise(CommandSender sender, Entity target, String disguise) throws Throwable {
        return parseDisguise(sender, target, "disguise", DisguiseUtilities.split(disguise), new DisguisePermissions(Bukkit.getConsoleSender(), "disguise"));
    }

    /**
     * Returns the disguise if it all parsed correctly. Returns a exception with a complete message if it didn't. The
     * commandsender is purely used for checking permissions. Would defeat the purpose otherwise. To reach this
     * point, the
     * disguise has been feed a proper disguisetype.
     */
    public static Disguise parseDisguise(CommandSender sender, Entity target, String permNode, String[] args, DisguisePermissions permissions)
        throws Throwable {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("DisguiseParser should not be called async!");
        }

        if (sender instanceof Player) {
            DisguiseUtilities.setCommandsUsed();
        }

        if (!permissions.hasPermissions()) {
            throw new DisguiseParseException(LibsMsg.NO_PERM);
        }

        if (args.length == 0) {
            throw new DisguiseParseException(LibsMsg.PARSE_NO_ARGS);
        }

        // How many args to skip due to the disugise being constructed
        // Time to start constructing the disguise.
        // We will need to check between all 3 kinds of disguises
        int toSkip = 1;
        ArrayList<String> usedOptions = new ArrayList<>();
        Disguise disguise = null;
        DisguisePerm disguisePerm;
        String name;
        boolean customName = false;

        if (args[0].startsWith("@")) {
            if (sender.hasPermission("libsdisguises.disguise.disguiseclone")) {
                disguise = DisguiseUtilities.getClonedDisguise(args[0].toLowerCase(Locale.ENGLISH));

                if (disguise == null) {
                    throw new DisguiseParseException(LibsMsg.PARSE_NO_REF, args[0]);
                }
            } else {
                throw new DisguiseParseException(LibsMsg.PARSE_NO_PERM_REF);
            }

            disguisePerm = new DisguisePerm(disguise.getType());
            name = disguise.getDisguiseName();
            customName = disguise.isCustomDisguiseName();

            if (disguisePerm.isUnknown()) {
                throw new DisguiseParseException(LibsMsg.PARSE_CANT_DISG_UNKNOWN);
            }

            if (disguisePerm.getEntityType() == null) {
                throw new DisguiseParseException(LibsMsg.PARSE_CANT_LOAD);
            }

            if (!permissions.isAllowedDisguise(disguisePerm)) {
                throw new DisguiseParseException(LibsMsg.NO_PERM_DISGUISE);
            }
        } else {
            disguisePerm = getDisguisePerm(args[0]);

            if (disguisePerm == null) {
                throw new DisguiseParseException(LibsMsg.PARSE_DISG_NO_EXIST, args[0]);
            }

            name = disguisePerm.toReadable();

            if (disguisePerm.getType().isCustom()) {
                ModdedEntity ent = ModdedManager.getModdedEntity(disguisePerm.toReadable());

                if (ent == null) {
                    throw new DisguiseParseException(LibsMsg.PARSE_CANT_DISG_UNKNOWN);
                }

                disguise = new ModdedDisguise(ent);
                customName = true;
            }

            Entry<DisguisePerm, String> customDisguise = DisguiseConfig.getRawCustomDisguise(args[0]);

            if (customDisguise != null) {
                // TODO Doesn't this mean we can't add args to our custom disguise on /disguise?
                // Need to add user defined args for the custom disguise
                args = DisguiseUtilities.split(customDisguise.getValue());
                name = customDisguise.getKey().toReadable();
                customName = true;
            }

            args = parsePlaceholders(args, sender == null ? target : sender, target);

            if (disguisePerm.isUnknown()) {
                throw new DisguiseParseException(LibsMsg.PARSE_CANT_DISG_UNKNOWN);
            }

            if (disguisePerm.getEntityType() == null) {
                throw new DisguiseParseException(LibsMsg.PARSE_CANT_LOAD);
            }

            if (!permissions.isAllowedDisguise(disguisePerm)) {
                throw new DisguiseParseException(LibsMsg.NO_PERM_DISGUISE);
            }

            HashMap<String, HashMap<String, Boolean>> disguiseOptions = DisguisePermissions.getDisguiseOptions(sender, permNode, disguisePerm);

            if (disguise == null) {
                if (disguisePerm.isPlayer()) {
                    // If he is doing a player disguise
                    if (args.length == 1) {
                        // He needs to give the player name
                        throw new DisguiseParseException(LibsMsg.PARSE_SUPPLY_PLAYER);
                    } else {
                        // If they can't use this name, throw error
                        if (!DisguisePermissions.hasPermissionOption(disguiseOptions, "setname", args[1].toLowerCase(Locale.ENGLISH))) {
                            if (!args[1].equalsIgnoreCase(sender.getName()) ||
                                !DisguisePermissions.hasPermissionOption(disguiseOptions, "setname", "themselves")) {
                                throw new DisguiseParseException(LibsMsg.PARSE_NO_PERM_NAME);
                            }
                        }

                        args[1] = args[1].replace("\\_", " ");

                        if (DisguiseConfig.isArmorstandsName() && !sender.hasPermission("libsdisguises.multiname")) {
                            args[1] = DisguiseUtilities.quoteNewLine(args[1]);
                        }

                        // Construct the player disguise
                        disguise = new PlayerDisguise(DisguiseUtilities.translateAlternateColorCodes(args[1]));

                        if (!customName) {
                            name = ((PlayerDisguise) disguise).getName();
                        }

                        toSkip++;
                    }
                } else if (disguisePerm.isMob()) { // Its a mob, use the mob constructor
                    if (args.length > 1) {
                        boolean adult = true;

                        if (args[1].equalsIgnoreCase(TranslateType.DISGUISE_OPTIONS.get("baby")) ||
                            args[1].equalsIgnoreCase(TranslateType.DISGUISE_OPTIONS.get("adult"))) {
                            usedOptions.add("setbaby");
                            doCheck(sender, permissions, disguisePerm, usedOptions);
                            adult = args[1].equalsIgnoreCase(TranslateType.DISGUISE_OPTIONS.get("adult"));

                            toSkip++;
                            disguise = new MobDisguise(disguisePerm.getType(), adult);
                        } else {
                            disguise = new MobDisguise(disguisePerm.getType());
                        }
                    } else {
                        disguise = new MobDisguise(disguisePerm.getType());
                    }
                } else if (disguisePerm.isMisc()) {
                    // Its a misc, we are going to use the MiscDisguise constructor.
                    ItemStack itemStack = new ItemStack(Material.STONE);
                    // The steps I go through for 1.12..
                    Object blockData = null;
                    int miscId = -1;

                    if (args.length > 1) {
                        switch (disguisePerm.getType()) {
                            case FALLING_BLOCK:
                            case DROPPED_ITEM:
                                ParamInfo info;

                                try {
                                    if (disguisePerm.getType() == DisguiseType.FALLING_BLOCK) {
                                        if (NmsVersion.v1_13.isSupported() && args[1].contains("[")) {
                                            info = ParamInfoManager.getParamInfo(BlockData.class);
                                            blockData = info.fromString(new ArrayList<>(Collections.singletonList(args[1])));
                                        } else {
                                            info = ParamInfoManager.getParamInfoItemBlock();

                                            itemStack = (ItemStack) info.fromString(new ArrayList<>(Collections.singletonList(args[1])));
                                        }
                                    } else {
                                        info = ParamInfoManager.getParamInfo(ItemStack.class);

                                        itemStack = (ItemStack) info.fromString(new ArrayList<>(Collections.singletonList(args[1])));
                                    }
                                } catch (Exception ex) {
                                    break;
                                }

                                String optionName;

                                if (disguisePerm.getType() == DisguiseType.FALLING_BLOCK) {
                                    optionName = "setblock";
                                } else {
                                    optionName = "setitemstack";
                                }

                                usedOptions.add(optionName);
                                doCheck(sender, permissions, disguisePerm, usedOptions);
                                String itemName = itemStack == null ? "null" : itemStack.getType().name().toLowerCase(Locale.ENGLISH);

                                if (!DisguisePermissions.hasPermissionOption(disguiseOptions, optionName, itemName)) {
                                    throw new DisguiseParseException(LibsMsg.PARSE_NO_PERM_PARAM, itemName, disguisePerm.toReadable());
                                }

                                toSkip++;

                                break;
                            case PAINTING:
                            case SPLASH_POTION:
                                if (!isInteger(args[1])) {
                                    break;
                                }

                                miscId = Integer.parseInt(args[1]);
                                toSkip++;

                                if (disguisePerm.getType() == DisguiseType.PAINTING) {
                                    optionName = "setpainting";
                                } else {
                                    optionName = "setpotionid";
                                }

                                usedOptions.add(optionName);

                                doCheck(sender, permissions, disguisePerm, usedOptions);

                                if (!DisguisePermissions.hasPermissionOption(disguiseOptions, optionName, miscId + "")) {
                                    throw new DisguiseParseException(LibsMsg.PARSE_NO_PERM_PARAM, miscId + "", disguisePerm.toReadable());
                                }
                                break;
                            default:
                                break;
                        }
                    }

                    // Construct the disguise
                    if (disguisePerm.getType() == DisguiseType.DROPPED_ITEM || disguisePerm.getType() == DisguiseType.FALLING_BLOCK) {
                        disguise = new MiscDisguise(disguisePerm.getType(), itemStack);

                        if (blockData != null && disguisePerm.getType() == DisguiseType.FALLING_BLOCK) {
                            ((FallingBlockWatcher) disguise.getWatcher()).setBlockData((BlockData) blockData);
                        }

                        if (!customName) {
                            name = disguise.getDisguiseName();
                        }
                    } else {
                        disguise = new MiscDisguise(disguisePerm.getType(), miscId);
                    }
                }
            }
        }

        disguise.setDisguiseName(name);
        disguise.setCustomDisguiseName(customName);

        // Copy strings to their new range
        String[] newArgs = new String[args.length - toSkip];
        System.arraycopy(args, toSkip, newArgs, 0, args.length - toSkip);

        callMethods(sender, disguise, permissions, disguisePerm, usedOptions, newArgs, permNode);

        if (sender instanceof Player && target instanceof Player && "%%__USER__%%".equals("15" + "92") && ThreadLocalRandom.current().nextBoolean()) {
            ((TargetedDisguise) disguise).setDisguiseTarget(TargetedDisguise.TargetType.HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS);
            ((TargetedDisguise) disguise).addPlayer((Player) sender);
        }

        // Alright. We've constructed our disguise.
        return disguise;
    }

    public static void callMethods(CommandSender sender, Disguise disguise, DisguisePermissions disguisePermission, DisguisePerm disguisePerm,
                                   Collection<String> usedOptions, String[] args, String permNode) throws Throwable {
        WatcherMethod[] methods = ParamInfoManager.getDisguiseWatcherMethods(disguise.getWatcher().getClass(), true);
        List<String> list = new ArrayList<>(Arrays.asList(args));
        HashMap<String, HashMap<String, Boolean>> disguiseOptions = DisguisePermissions.getDisguiseOptions(sender, permNode, disguisePerm);

        for (int argIndex = 0; argIndex < args.length; argIndex++) {
            // This is the method name they provided
            String methodNameProvided = list.remove(0);
            // Translate the name they provided, to a name we recognize
            String methodNameJava = TranslateType.DISGUISE_OPTIONS.reverseGet(methodNameProvided);
            // The method we'll use
            WatcherMethod methodToUse = null;
            Object valueToSet = null;
            DisguiseParseException parseException = null;

            if (!list.isEmpty() && methodNameProvided.equalsIgnoreCase("setCustomData") && (sender == null || sender.isOp())) {
                argIndex++;
                String data = list.remove(0);
                Map<String, String> deserial = DisguiseUtilities.getGson().fromJson(data, LinkedHashMap.class);

                for (Entry<String, String> entry : deserial.entrySet()) {
                    String val = entry.getValue();

                    if (!val.contains(":")) {
                        disguise.addCustomData(entry.getKey(), null);
                        continue;
                    }

                    String className = val.substring(0, val.indexOf(":"));
                    val = val.substring(className.length() + 1);

                    disguise.addCustomData(entry.getKey(), DisguiseUtilities.getGson().fromJson(val, DisguiseMethods.parseType(className)));
                }

                continue;
            }

            for (WatcherMethod method : methods) {
                if (!method.getName().equalsIgnoreCase(methodNameJava)) {
                    continue;
                }

                ParamInfo paramInfo = ParamInfoManager.getParamInfo(method);

                try {
                    // Store how many args there were before calling the param
                    int argCount = list.size();

                    if (argCount < paramInfo.getMinArguments()) {
                        throw new DisguiseParseException(LibsMsg.PARSE_NO_OPTION_VALUE, TranslateType.DISGUISE_OPTIONS.reverseGet(method.getName()));
                    }

                    valueToSet = paramInfo.fromString(list);

                    if (valueToSet == null && !paramInfo.canReturnNull()) {
                        throw new IllegalStateException();
                    }

                    // Skip ahead as many args as were consumed on successful parse
                    argIndex += argCount - list.size();

                    methodToUse = method;
                    // We've found a method which will accept a valid value, break
                    break;
                } catch (DisguiseParseException ex) {
                    parseException = ex;
                } catch (Exception ignored) {
                    parseException =
                        new DisguiseParseException(LibsMsg.PARSE_EXPECTED_RECEIVED, paramInfo.getDescriptiveName(), list.isEmpty() ? null : list.get(0),
                            TranslateType.DISGUISE_OPTIONS.reverseGet(method.getName()));
                }
            }

            if (methodToUse == null) {
                if (parseException != null) {
                    throw parseException;
                }

                throw new DisguiseParseException(LibsMsg.PARSE_OPTION_NA, methodNameProvided);
            }

            if (!usedOptions.contains(methodToUse.getName().toLowerCase(Locale.ENGLISH))) {
                usedOptions.add(methodToUse.getName().toLowerCase(Locale.ENGLISH));
            }

            doCheck(sender, disguisePermission, disguisePerm, usedOptions);

            if (!disguiseOptions.isEmpty()) {
                String stringValue = ParamInfoManager.toString(valueToSet);

                if (!DisguisePermissions.hasPermissionOption(disguiseOptions, methodToUse.getName(), stringValue)) {
                    throw new DisguiseParseException(LibsMsg.PARSE_NO_PERM_PARAM, stringValue, disguisePerm.toReadable());
                }
            }

            if (DisguiseConfig.isArmorstandsName() && ((methodToUse.getName().equals("setName") && disguise.isPlayerDisguise()) ||
                (DisguiseConfig.isOverrideCustomNames() && methodToUse.getName().equals("setCustomName"))) &&
                !sender.hasPermission("libsdisguises.multiname")) {
                valueToSet = DisguiseUtilities.quoteNewLine((String) valueToSet);
            }

            MethodHandle handle = methodToUse.getMethod();

            if (FlagWatcher.class.isAssignableFrom(methodToUse.getWatcherClass())) {
                handle = handle.bindTo(disguise.getWatcher());
            } else {
                handle = handle.bindTo(disguise);
            }

            handle.invoke(valueToSet);
        }
    }
}
