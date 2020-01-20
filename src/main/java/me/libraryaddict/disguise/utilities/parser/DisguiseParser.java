package me.libraryaddict.disguise.utilities.parser;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.gson.Gson;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.params.ParamInfo;
import me.libraryaddict.disguise.utilities.parser.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class DisguiseParser {
    /**
     * <Setter, <Getter, DefaultValue>>
     */
    private static HashMap<Method, Map.Entry<Method, Object>> defaultWatcherValues = new HashMap<>();

    public static void createDefaultMethods() {
        try {
            for (DisguiseType type : DisguiseType.values()) {
                Disguise disguise;

                if (type.isMisc()) {
                    disguise = new MiscDisguise(type);
                } else if (type.isMob()) {
                    disguise = new MobDisguise(type);
                } else {
                    disguise = new PlayerDisguise("Foobar");
                }

                FlagWatcher watcher = type.getWatcherClass().getConstructor(Disguise.class).newInstance(disguise);

                Method[] methods = ParamInfoManager.getDisguiseWatcherMethods(watcher.getClass());

                for (Method setMethod : methods) {
                    // Invalidate methods that can't be handled normally
                    if (setMethod.getName().equals("addPotionEffect") || (setMethod.getName().equals("setSkin") &&
                            setMethod.getParameterTypes()[0] == String.class) ||
                            (setMethod.getName().equals("setTarget") &&
                                    setMethod.getParameterTypes()[0] != int.class) ||
                            (setMethod.getName().equals("setItemInMainHand") &&
                                    setMethod.getParameterTypes()[0] == Material.class)) {
                        continue;
                    }

                    String getName = setMethod.getName().substring(3); // Remove 'set'

                    if (getName.equals("HasNectar")) {
                        getName = "hasNectar";
                    } else if (getName.equals("HasStung")) {
                        getName = "hasStung";
                    } else if (setMethod.getParameterTypes()[0].isAssignableFrom(boolean.class)) {
                        getName = "is" + getName;
                    } else {
                        getName = "get" + getName;
                    }

                    Method getMethod = null;

                    for (Method m : setMethod.getDeclaringClass().getDeclaredMethods()) {
                        if (!m.getName().equals(getName)) {
                            continue;
                        }

                        if (m.getParameterTypes().length > 0 || m.getReturnType() != setMethod.getParameterTypes()[0]) {
                            continue;
                        }

                        getMethod = m;
                        break;
                    }

                    if (getMethod == null) {
                        DisguiseUtilities.getLogger().severe(String
                                .format("No such method '%s' when looking for the companion of '%s' in '%s'", getName,
                                        setMethod.getName(), setMethod.getDeclaringClass().getSimpleName()));
                        continue;
                    }

                    Object defaultValue = null;

                    // Value is randomish so shouldn't be checked, should always specify value when setting
                    if (!setMethod.isAnnotationPresent(RandomDefaultValue.class)) {
                        Object invokeWith = watcher;

                        if (!FlagWatcher.class.isAssignableFrom(getMethod.getDeclaringClass())) {
                            invokeWith = disguise;
                        }

                        defaultValue = getMethod.invoke(invokeWith);
                    }

                    addWatcherDefault(setMethod, getMethod, defaultValue);
                }
            }
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<Method, Entry<Method, Object>> getMethodDefaults() {
        return defaultWatcherValues;
    }

    public static String parseToString(Disguise disguise) {
        return parseToString(disguise, true);
    }

    /**
     * Not outputting skin information is not garanteed to display the correct player name
     */
    public static String parseToString(Disguise disguise, boolean outputSkinData) {
        try {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(disguise.getType().name());

            if (disguise.isPlayerDisguise()) {
                stringBuilder.append(" ").append(DisguiseUtilities.quote(((PlayerDisguise) disguise).getName()));
            }

            for (Method m : ParamInfoManager.getDisguiseWatcherMethods(disguise.getType().getWatcherClass())) {
                // Special handling for this method
                if (m.getName().equals("addPotionEffect")) {
                    PotionEffectType[] types = (PotionEffectType[]) m.getDeclaringClass().getMethod("getPotionEffects")
                            .invoke(disguise.getWatcher());

                    for (PotionEffectType type : types) {
                        if (type == null) {
                            continue;
                        }

                        stringBuilder.append(" ").append(m.getName()).append(" ").append(type.getName());
                    }
                } else {
                    Entry<Method, Object> entry = defaultWatcherValues.get(m);

                    if (entry == null) {
                        continue;
                    }

                    Object invokeWith = m.getDeclaringClass().isInstance(disguise) ? disguise : disguise.getWatcher();

                    Object ourValue = entry.getKey().invoke(invokeWith);

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
                        if (!m.isAnnotationPresent(RandomDefaultValue.class) &&
                                Objects.deepEquals(entry.getValue(), ourValue)) {
                            continue;
                        }
                    }

                    stringBuilder.append(" ").append(m.getName());

                    if (ourValue instanceof Boolean && (Boolean) ourValue) {
                        continue;
                    }

                    String valueString;

                    if (ourValue != null) {
                        valueString = ParamInfoManager.getParamInfo(ourValue.getClass()).toString(ourValue);

                        valueString = DisguiseUtilities.quote(valueString);
                    } else {
                        valueString = "null";
                    }

                    stringBuilder.append(" ").append(valueString);
                }
            }

            return stringBuilder.toString();
        }
        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void addWatcherDefault(Method setMethod, Method getMethod, Object object) {
        Map.Entry<Method, Object> entry = new HashMap.SimpleEntry<>(getMethod, object);

        if (defaultWatcherValues.containsKey(setMethod)) {
            Object dObj = defaultWatcherValues.get(setMethod);

            if (!Objects.deepEquals(defaultWatcherValues.get(setMethod).getValue(), object)) {
                throw new IllegalStateException(String.format("%s has conflicting values!", setMethod.getName()));
            }

            return;
        }

        defaultWatcherValues.put(setMethod, entry);
    }

    private static void doCheck(CommandSender sender, DisguisePermissions permissions, DisguisePerm disguisePerm,
            Collection<String> usedOptions) throws DisguiseParseException {

        if (!permissions.isAllowedDisguise(disguisePerm, usedOptions)) {
            throw new DisguiseParseException(LibsMsg.D_PARSE_NOPERM,
                    usedOptions.stream().reduce((first, second) -> second).orElse(null));
        }
    }

    private static HashMap<String, Boolean> getDisguiseOptions(CommandSender sender, String permNode,
            DisguisePerm type) {
        switch (type.getType()) {
            case PLAYER:
            case FALLING_BLOCK:
            case PAINTING:
            case SPLASH_POTION:
            case FISHING_HOOK:
            case DROPPED_ITEM:
                HashMap<String, Boolean> returns = new HashMap<>();

                String beginning = "libsdisguises.options." + permNode.toLowerCase() + ".";

                for (PermissionAttachmentInfo permission : sender.getEffectivePermissions()) {
                    String lowerPerm = permission.getPermission().toLowerCase();

                    if (lowerPerm.startsWith(beginning)) {
                        String[] split = lowerPerm.substring(beginning.length()).split("\\.");

                        if (split.length > 1) {
                            if (split[0].replace("_", "").equals(type.toReadable().toLowerCase().replace(" ", ""))) {
                                for (int i = 1; i < split.length; i++) {
                                    returns.put(split[i], permission.getValue());
                                }
                            }
                        }
                    }
                }

                return returns;
            default:
                return new HashMap<>();
        }
    }

    public static DisguisePerm getDisguisePerm(String name) {
        for (DisguisePerm perm : getDisguisePerms()) {
            if (!perm.toReadable().replaceAll("[ |_]", "").equalsIgnoreCase(name.replaceAll("[ |_]", "")))
                continue;

            return perm;
        }

        if (name.equalsIgnoreCase("p"))
            return getDisguisePerm(DisguiseType.PLAYER.toReadable());

        return null;
    }

    public static DisguisePerm[] getDisguisePerms() {
        DisguisePerm[] perms = new DisguisePerm[DisguiseType.values().length +
                DisguiseConfig.getCustomDisguises().size()];
        int i = 0;

        for (DisguiseType disguiseType : DisguiseType.values()) {
            perms[i++] = new DisguisePerm(disguiseType);
        }

        for (Entry<DisguisePerm, String> entry : DisguiseConfig.getCustomDisguises().entrySet()) {
            perms[i++] = entry.getKey();
        }

        return perms;
    }

    /**
     * Get perms for the node. Returns a hashmap of allowed disguisetypes and their options
     */
    public static DisguisePermissions getPermissions(CommandSender sender, String commandName) {
        return new DisguisePermissions(sender, commandName);
    }

    private static boolean isDouble(String string) {
        try {
            Float.parseFloat(string);
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

    private static boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

    /**
     * Returns true if the string is found in the map, or it's not a whitelisted setup
     * <p>
     * Returns if command user can access the disguise creation permission type
     */
    private static boolean hasPermissionOption(HashMap<String, Boolean> disguiseOptions, String string) {
        string = string.toLowerCase();
        // If no permissions were defined, return true
        if (disguiseOptions.isEmpty()) {
            return true;
        }

        // If they were explictly defined, can just return the value
        if (disguiseOptions.containsKey(string)) {
            return disguiseOptions.get(string);
        }

        // If there is at least one whitelisted value, then they needed the whitelist to use it
        return !disguiseOptions.containsValue(true);
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
        return parsePlaceholders(args, getName(user), getSkin(user), getName(target), DisguiseParser.getSkin(target),
                getEntityEquipment(user), getEntityEquipment(target));
    }

    private static EntityEquipment getEntityEquipment(CommandSender entity) {
        return entity instanceof LivingEntity ? ((LivingEntity) entity).getEquipment() : null;
    }

    public static String[] parsePlaceholders(String[] args, String userName, String userSkin, String targetName,
            String targetSkin, EntityEquipment equip, EntityEquipment targetEquip) {

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            arg = replace(arg, "%user-name%", userName);
            arg = replace(arg, "%user-skin%", userSkin);
            arg = replace(arg, "%target-name%", targetName);
            arg = replace(arg, "%target-skin%", targetSkin);
            arg = replace(arg, "%held-item%", equip == null ? null : equip.getItemInMainHand());
            arg = replace(arg, "%offhand-item%", equip == null ? null : equip.getItemInOffHand());
            arg = replace(arg, "%armor%", equip == null ? null : equip.getArmorContents());
            arg = replace(arg, "%helmet%", equip == null ? null : equip.getHelmet());
            arg = replace(arg, "%chestplate%", equip == null ? null : equip.getChestplate());
            arg = replace(arg, "%leggings%%", equip == null ? null : equip.getLeggings());
            arg = replace(arg, "%boots%", equip == null ? null : equip.getBoots());

            arg = replace(arg, "%target-held-item%", targetEquip == null ? null : targetEquip.getItemInMainHand());
            arg = replace(arg, "%target-offhand-item%", targetEquip == null ? null : targetEquip.getItemInOffHand());
            arg = replace(arg, "%target-armor%", targetEquip == null ? null : targetEquip.getArmorContents());
            arg = replace(arg, "%target-helmet%", targetEquip == null ? null : targetEquip.getHelmet());
            arg = replace(arg, "%target-chestplate%", targetEquip == null ? null : targetEquip.getChestplate());
            arg = replace(arg, "%target-leggings%%", targetEquip == null ? null : targetEquip.getLeggings());
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
        string = string.toLowerCase();

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
    public static Disguise parseTestDisguise(CommandSender sender, String permNode, String[] args,
            DisguisePermissions permissions) throws DisguiseParseException, IllegalAccessException,
            InvocationTargetException {

        // Clone array so original array isn't modified
        args = Arrays.copyOf(args, args.length);

        String skin = "{\"id\":\"a149f81bf7844f8987c554afdd4db533\",\"name\":\"libraryaddict\"," + "\"properties\":[]}";
        // Fill in fake data
        args = parsePlaceholders(args, "libraryaddict", skin, "libraryaddict", skin, null, null);

        // Parse disguise
        return parseDisguise(sender, null, permNode, args, permissions);
    }

    public static Disguise parseDisguise(
            String disguise) throws IllegalAccessException, InvocationTargetException, DisguiseParseException {
        return parseDisguise(Bukkit.getConsoleSender(), null, disguise);
    }

    public static Disguise parseDisguise(CommandSender sender, Entity target,
            String disguise) throws IllegalAccessException, InvocationTargetException, DisguiseParseException {
        return parseDisguise(sender, target, "disguise", DisguiseUtilities.split(disguise),
                new DisguisePermissions(Bukkit.getConsoleSender(), "disguise"));
    }

    /**
     * Returns the disguise if it all parsed correctly. Returns a exception with a complete message if it didn't. The
     * commandsender is purely used for checking permissions. Would defeat the purpose otherwise. To reach this
     * point, the
     * disguise has been feed a proper disguisetype.
     */
    public static Disguise parseDisguise(CommandSender sender, Entity target, String permNode, String[] args,
            DisguisePermissions permissions) throws DisguiseParseException, IllegalAccessException,
            InvocationTargetException {
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

        if (args[0].startsWith("@")) {
            if (sender.hasPermission("libsdisguises.disguise.disguiseclone")) {
                disguise = DisguiseUtilities.getClonedDisguise(args[0].toLowerCase());

                if (disguise == null) {
                    throw new DisguiseParseException(LibsMsg.PARSE_NO_REF, args[0]);
                }
            } else {
                throw new DisguiseParseException(LibsMsg.PARSE_NO_PERM_REF);
            }

            disguisePerm = new DisguisePerm(disguise.getType());

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
            Entry<DisguisePerm, String> customDisguise = DisguiseConfig.getRawCustomDisguise(args[0]);

            if (customDisguise != null) {
                args = DisguiseUtilities.split(customDisguise.getValue());
            }

            args = parsePlaceholders(args, sender, target);

            if (disguisePerm == null) {
                throw new DisguiseParseException(LibsMsg.PARSE_DISG_NO_EXIST, args[0]);
            }

            if (disguisePerm.isUnknown()) {
                throw new DisguiseParseException(LibsMsg.PARSE_CANT_DISG_UNKNOWN);
            }

            if (disguisePerm.getEntityType() == null) {
                throw new DisguiseParseException(LibsMsg.PARSE_CANT_LOAD);
            }

            if (!permissions.isAllowedDisguise(disguisePerm)) {
                throw new DisguiseParseException(LibsMsg.NO_PERM_DISGUISE);
            }

            HashMap<String, Boolean> disguiseOptions = getDisguiseOptions(sender, permNode, disguisePerm);

            if (disguise == null) {
                if (disguisePerm.isPlayer()) {
                    // If he is doing a player disguise
                    if (args.length == 1) {
                        // He needs to give the player name
                        throw new DisguiseParseException(LibsMsg.PARSE_SUPPLY_PLAYER);
                    } else {
                        // If they can't use this name, throw error
                        if (!hasPermissionOption(disguiseOptions, args[1].toLowerCase())) {
                            throw new DisguiseParseException(LibsMsg.PARSE_NO_PERM_NAME);
                        }

                        args[1] = args[1].replace("\\_", " ");

                        // Construct the player disguise
                        disguise = new PlayerDisguise(ChatColor.translateAlternateColorCodes('&', args[1]));
                        toSkip++;
                    }
                } else if (disguisePerm.isMob()) { // Its a mob, use the mob constructor
                    boolean adult = true;

                    if (args.length > 1) {
                        if (args[1].equalsIgnoreCase(TranslateType.DISGUISE_OPTIONS.get("baby")) ||
                                args[1].equalsIgnoreCase(TranslateType.DISGUISE_OPTIONS.get("adult"))) {
                            usedOptions.add("setbaby");
                            doCheck(sender, permissions, disguisePerm, usedOptions);
                            adult = args[1].equalsIgnoreCase(TranslateType.DISGUISE_OPTIONS.get("adult"));

                            toSkip++;
                        }
                    }

                    disguise = new MobDisguise(disguisePerm.getType(), adult);
                } else if (disguisePerm.isMisc()) {
                    // Its a misc, we are going to use the MiscDisguise constructor.
                    ItemStack itemStack = new ItemStack(Material.STONE);
                    int miscId = -1;

                    if (args.length > 1) {
                        switch (disguisePerm.getType()) {
                            case FALLING_BLOCK:
                            case DROPPED_ITEM:
                                Material material = null;

                                for (Material mat : Material.values()) {
                                    if (!mat.name().replace("_", "").equalsIgnoreCase(args[1].replace("_", ""))) {
                                        continue;
                                    }

                                    material = mat;
                                    break;
                                }

                                if (material == null) {
                                    break;
                                }

                                itemStack = new ItemStack(material);

                                if (!hasPermissionOption(disguiseOptions, itemStack.getType().name().toLowerCase())) {
                                    throw new DisguiseParseException(LibsMsg.PARSE_NO_PERM_PARAM,
                                            itemStack.getType().name(), disguisePerm.toReadable());
                                }

                                toSkip++;

                                if (disguisePerm.getType() == DisguiseType.FALLING_BLOCK) {
                                    usedOptions.add("setblock");
                                } else {
                                    usedOptions.add("setitemstack");
                                }

                                doCheck(sender, permissions, disguisePerm, usedOptions);
                                break;
                            case PAINTING:
                            case SPLASH_POTION:
                                if (!isInteger(args[1])) {
                                    break;
                                }

                                miscId = Integer.parseInt(args[1]);
                                toSkip++;

                                if (!hasPermissionOption(disguiseOptions, miscId + "")) {
                                    throw new DisguiseParseException(LibsMsg.PARSE_NO_PERM_PARAM, miscId + "",
                                            disguisePerm.toReadable());
                                }

                                if (disguisePerm.getType() == DisguiseType.PAINTING) {
                                    usedOptions.add("setpainting");
                                } else {
                                    usedOptions.add("setpotionid");
                                }

                                doCheck(sender, permissions, disguisePerm, usedOptions);
                                break;
                            default:
                                break;
                        }
                    }

                    // Construct the disguise
                    if (disguisePerm.getType() == DisguiseType.DROPPED_ITEM ||
                            disguisePerm.getType() == DisguiseType.FALLING_BLOCK) {
                        disguise = new MiscDisguise(disguisePerm.getType(), itemStack);
                    } else {
                        disguise = new MiscDisguise(disguisePerm.getType(), miscId);
                    }
                }
            }
        }

        // Copy strings to their new range
        String[] newArgs = new String[args.length - toSkip];
        System.arraycopy(args, toSkip, newArgs, 0, args.length - toSkip);

        callMethods(sender, disguise, permissions, disguisePerm, usedOptions, newArgs, permNode);

        // Alright. We've constructed our disguise.
        return disguise;
    }

    public static void callMethods(CommandSender sender, Disguise disguise, DisguisePermissions disguisePermission,
            DisguisePerm disguisePerm, Collection<String> usedOptions, String[] args,
            String permNode) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            DisguiseParseException {
        Method[] methods = ParamInfoManager.getDisguiseWatcherMethods(disguise.getWatcher().getClass());
        List<String> list = new ArrayList<>(Arrays.asList(args));
        HashMap<String, Boolean> disguiseOptions = null;

        for (int argIndex = 0; argIndex < args.length; argIndex++) {
            // This is the method name they provided
            String methodNameProvided = list.remove(0);
            // Translate the name they provided, to a name we recognize
            String methodNameJava = TranslateType.DISGUISE_OPTIONS.reverseGet(methodNameProvided);
            // The method we'll use
            Method methodToUse = null;
            Object valueToSet = null;
            DisguiseParseException parseException = null;

            for (Method method : methods) {
                if (!method.getName().equalsIgnoreCase(methodNameJava)) {
                    continue;
                }

                Class paramType = method.getParameterTypes()[0];

                ParamInfo paramInfo = ParamInfoManager.getParamInfo(paramType);

                try {
                    // Store how many args there were before calling the param
                    int argCount = list.size();

                    if (argCount < paramInfo.getMinArguments()) {
                        throw new DisguiseParseException(LibsMsg.PARSE_NO_OPTION_VALUE,
                                TranslateType.DISGUISE_OPTIONS.reverseGet(method.getName()));
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
                }
                catch (DisguiseParseException ex) {
                    parseException = ex;
                }
                catch (Exception ignored) {
                    parseException = new DisguiseParseException(LibsMsg.PARSE_EXPECTED_RECEIVED,
                            paramInfo.getDescriptiveName(), list.isEmpty() ? null : list.get(0),
                            TranslateType.DISGUISE_OPTIONS.reverseGet(method.getName()));
                }
            }

            if (methodToUse == null) {
                if (parseException != null) {
                    throw parseException;
                }

                throw new DisguiseParseException(LibsMsg.PARSE_OPTION_NA, methodNameProvided);
            }

            if (!usedOptions.contains(methodToUse.getName().toLowerCase())) {
                usedOptions.add(methodToUse.getName().toLowerCase());
            }

            if (methodToUse.getName().equalsIgnoreCase("setpainting") ||
                    methodToUse.getName().equalsIgnoreCase("setpotionid") ||
                    methodToUse.getName().equalsIgnoreCase("setitemstack") ||
                    methodToUse.getName().equalsIgnoreCase("setblock")) {
                if (disguiseOptions == null) {
                    disguiseOptions = getDisguiseOptions(sender, permNode, disguisePerm);
                }

                String stringValue = ParamInfoManager.toString(valueToSet);

                if (!hasPermissionOption(disguiseOptions, stringValue)) {
                    throw new DisguiseParseException(LibsMsg.PARSE_NO_PERM_PARAM, stringValue,
                            disguisePerm.toReadable());
                }
            }

            doCheck(sender, disguisePermission, disguisePerm, usedOptions);

            if (FlagWatcher.class.isAssignableFrom(methodToUse.getDeclaringClass())) {
                methodToUse.invoke(disguise.getWatcher(), valueToSet);
            } else {
                methodToUse.invoke(disguise, valueToSet);
            }
        }
    }
}
