package me.libraryaddict.disguise.utilities;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.RabbitType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

public abstract class BaseDisguiseCommand implements CommandExecutor {

    public class DisguiseParseException extends Exception {

        private static final long serialVersionUID = 1276971370793124510L;

        public DisguiseParseException() {
            super();
        }

        public DisguiseParseException(String string) {
            super(string);
        }
    }

    protected ArrayList<String> getAllowedDisguises(HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> hashMap) {
        ArrayList<String> allowedDisguises = new ArrayList<>();
        for (DisguiseType type : hashMap.keySet()) {
            allowedDisguises.add(type.toReadable().replace(" ", "_"));
        }
        Collections.sort(allowedDisguises, String.CASE_INSENSITIVE_ORDER);
        return allowedDisguises;
    }

    protected HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> getPermissions(CommandSender sender) {
        return getPermissions(sender, "libsdisguises." + getClass().getSimpleName().replace("Command", "").toLowerCase() + ".");
    }

    protected HashMap<String, Boolean> getDisguisePermission(CommandSender sender, DisguiseType type) {
        switch (type) {
            case PLAYER:
            case FALLING_BLOCK:
            case PAINTING:
            case SPLASH_POTION:
            case FISHING_HOOK:
            case DROPPED_ITEM:
                HashMap<String, Boolean> returns = new HashMap<>();
                String beginning = "libsdisguises.options." + getClass().getSimpleName().toLowerCase().replace("command", "") + ".";
                for (PermissionAttachmentInfo permission : sender.getEffectivePermissions()) {
                    String lowerPerm = permission.getPermission().toLowerCase();
                    if (lowerPerm.startsWith(beginning)) {
                        String[] split = lowerPerm.substring(beginning.length()).split("\\.");
                        if (split.length > 1) {
                            if (split[0].replace("_", "").equals(type.name().toLowerCase().replace("_", ""))) {
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

    protected Method[] getDisguiseWatcherMethods(Class<? extends FlagWatcher> watcherClass) {
        Method[] methods = watcherClass.getMethods();
        methods = Arrays.copyOf(methods, methods.length + 4);
        int i = 4;
        for (String methodName : new String[]{"setViewSelfDisguise", "setHideHeldItemFromSelf", "setHideArmorFromSelf",
            "setHearSelfDisguise"}) {
            try {
                methods[methods.length - i--] = Disguise.class.getMethod(methodName, boolean.class);
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
            }
        }
        return methods;
    }

    /**
     * Get perms for the node. Returns a hashmap of allowed disguisetypes and their options
     *
     * @param sender
     * @param permissionNode
     * @return
     */
    protected HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> getPermissions(CommandSender sender, String permissionNode) {
        HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> singleDisguises = new HashMap<>();
        HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> rangeDisguises = new HashMap<>();
        HashMap<String, Boolean> perms = new HashMap<>();

        for (PermissionAttachmentInfo permission : sender.getEffectivePermissions()) {
            String perm = permission.getPermission().toLowerCase();
            if (perm.startsWith(permissionNode) && (!perms.containsKey(perm) || !permission.getValue())) {
                perms.put(perm, permission.getValue());
            }
        }
        if (!perms.containsKey(permissionNode + "*") && sender.hasPermission(permissionNode + "*")) {
            perms.put(permissionNode + "*", true);
        }
        if (!perms.containsKey(permissionNode + "*.*") && sender.hasPermission(permissionNode + "*.*")) {
            perms.put(permissionNode + "*.*", true);
        }

        for (String perm : perms.keySet()) {
            if (perms.get(perm)) {
                perm = perm.substring(permissionNode.length());
                String disguiseType = perm.split("\\.")[0];
                DisguiseType dType = null;
                for (DisguiseType t : DisguiseType.values()) {
                    if (t.name().replace("_", "").equalsIgnoreCase(disguiseType.replace("_", ""))) {
                        dType = t;
                        break;
                    }
                }
                if (dType != null) {
                    HashMap<ArrayList<String>, Boolean> list;
                    if (singleDisguises.containsKey(dType)) {
                        list = singleDisguises.get(dType);
                    } else {
                        list = new HashMap<>();
                        singleDisguises.put(dType, list);
                    }
                    HashMap<ArrayList<String>, Boolean> map1 = getOptions(perm);
                    list.put(map1.keySet().iterator().next(), map1.values().iterator().next());
                } else {
                    for (DisguiseType type : DisguiseType.values()) {
                        HashMap<ArrayList<String>, Boolean> options = null;
                        Class entityClass = type.getEntityClass();
                        if (disguiseType.equals("mob")) {
                            if (type.isMob()) {
                                options = getOptions(perm);
                            }
                        } else if (disguiseType.equals("animal") || disguiseType.equals("animals")) {
                            if (Animals.class.isAssignableFrom(entityClass)) {
                                options = getOptions(perm);
                            }
                        } else if (disguiseType.equals("monster") || disguiseType.equals("monsters")) {
                            if (Monster.class.isAssignableFrom(entityClass)) {
                                options = getOptions(perm);
                            }
                        } else if (disguiseType.equals("misc")) {
                            if (type.isMisc()) {
                                options = getOptions(perm);
                            }
                        } else if (disguiseType.equals("ageable")) {
                            if (Ageable.class.isAssignableFrom(entityClass)) {
                                options = getOptions(perm);
                            }
                        } else if (disguiseType.equals("*")) {
                            options = getOptions(perm);
                        }
                        if (options != null) {
                            HashMap<ArrayList<String>, Boolean> list;
                            if (rangeDisguises.containsKey(type)) {
                                list = rangeDisguises.get(type);
                            } else {
                                list = new HashMap<>();
                                rangeDisguises.put(type, list);
                            }
                            HashMap<ArrayList<String>, Boolean> map1 = getOptions(perm);
                            list.put(map1.keySet().iterator().next(), map1.values().iterator().next());
                        }
                    }
                }
            }
        }
        for (String perm : perms.keySet()) {
            if (!perms.get(perm)) {
                perm = perm.substring(permissionNode.length());
                String disguiseType = perm.split("\\.")[0];
                DisguiseType dType = null;
                for (DisguiseType t : DisguiseType.values()) {
                    if (t.name().replace("_", "").equalsIgnoreCase(disguiseType.replace("_", ""))) {
                        dType = t;
                        break;
                    }
                }
                if (dType != null) {
                    singleDisguises.remove(dType);
                    rangeDisguises.remove(dType);
                } else {
                    for (DisguiseType type : DisguiseType.values()) {
                        boolean foundHim = false;
                        Class entityClass = type.getEntityClass();
                        if (disguiseType.equals("mob")) {
                            if (type.isMob()) {
                                foundHim = true;
                            }
                        } else if (disguiseType.equals("animal") || disguiseType.equals("animals")) {
                            if (Animals.class.isAssignableFrom(entityClass)) {
                                foundHim = true;
                            }
                        } else if (disguiseType.equals("monster") || disguiseType.equals("monsters")) {
                            if (Monster.class.isAssignableFrom(entityClass)) {
                                foundHim = true;
                            }
                        } else if (disguiseType.equals("misc")) {
                            if (type.isMisc()) {
                                foundHim = true;
                            }
                        } else if (disguiseType.equals("ageable")) {
                            if (Ageable.class.isAssignableFrom(entityClass)) {
                                foundHim = true;
                            }
                        } else if (disguiseType.equals("*")) {
                            foundHim = true;
                        }
                        if (foundHim) {
                            rangeDisguises.remove(type);
                        }
                    }
                }
            }
        }
        HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> map = new HashMap<>();
        for (DisguiseType type : DisguiseType.values()) {
            HashMap<ArrayList<String>, Boolean> temp = new HashMap<>();
            if (singleDisguises.containsKey(type)) {
                temp.putAll(singleDisguises.get(type));
            }
            if (rangeDisguises.containsKey(type)) {
                temp.putAll(rangeDisguises.get(type));
            }
            if (!temp.isEmpty()) {
                map.put(type, temp);
            }
        }
        return map;
    }

    private HashMap<ArrayList<String>, Boolean> getOptions(String perm) {
        ArrayList<String> list = new ArrayList<>();
        boolean isRemove = true;
        String[] split = perm.split("\\.");
        for (int i = 1; i < split.length; i++) {
            String option = split[i];
            boolean value = option.startsWith("-");
            if (value) {
                option = option.substring(1);
                isRemove = false;
            }
            if (option.equals("baby")) {
                option = "setbaby";
            }
            list.add(option);
        }
        HashMap<ArrayList<String>, Boolean> options = new HashMap<>();
        options.put(list, isRemove);
        return options;
    }

    protected boolean isDouble(String string) {
        try {
            Float.parseFloat(string);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    protected boolean isNumeric(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Returns the disguise if it all parsed correctly. Returns a exception with a complete message if it didn't. The commandsender is purely used for checking permissions. Would defeat the purpose otherwise. To reach this point, the disguise has been feed a proper disguisetype.
     *
     * @param sender
     * @param args
     * @param map
     * @return
     * @throws me.libraryaddict.disguise.utilities.BaseDisguiseCommand.DisguiseParseException
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.reflect.InvocationTargetException
     */
    protected Disguise parseDisguise(CommandSender sender, String[] args, HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> map) throws DisguiseParseException,
            IllegalAccessException, InvocationTargetException {
        if (map.isEmpty()) {
            throw new DisguiseParseException(ChatColor.RED + "You are forbidden to use this command.");
        }
        if (args.length == 0) {
            sendCommandUsage(sender, map);
            throw new DisguiseParseException();
        }
        // How many args to skip due to the disugise being constructed
        // Time to start constructing the disguise.
        // We will need to check between all 3 kinds of disguises
        int toSkip = 1;
        ArrayList<String> usedOptions = new ArrayList<>();
        Disguise disguise = null;
        HashMap<ArrayList<String>, Boolean> optionPermissions;
        if (args[0].startsWith("@")) {
            if (sender.hasPermission("libsdisguises.disguise.disguiseclone")) {
                disguise = DisguiseUtilities.getClonedDisguise(args[0].toLowerCase());
                if (disguise == null) {
                    throw new DisguiseParseException(ChatColor.RED + "Cannot find a disguise under the reference " + args[0]);
                }
            } else {
                throw new DisguiseParseException(ChatColor.RED + "You do not have perimssion to use disguise references!");
            }
            optionPermissions = (map.containsKey(disguise.getType()) ? map.get(disguise.getType())
                    : new HashMap<ArrayList<String>, Boolean>());
        } else {
            DisguiseType disguiseType = null;
            if (args[0].equalsIgnoreCase("p")) {
                disguiseType = DisguiseType.PLAYER;
            } else {
                for (DisguiseType type : DisguiseType.values()) {
                    if (args[0].equalsIgnoreCase(type.name()) || args[0].equalsIgnoreCase(type.name().replace("_", ""))) {
                        disguiseType = type;
                        break;
                    }
                }
            }
            if (disguiseType == null) {
                throw new DisguiseParseException(ChatColor.RED + "Error! The disguise " + ChatColor.GREEN + args[0]
                        + ChatColor.RED + " doesn't exist!");
            }
            if (disguiseType.isUnknown()) {
                throw new DisguiseParseException(ChatColor.RED + "Error! You cannot disguise as " + ChatColor.GREEN + "Unknown!");
            }
            if (disguiseType.getEntityType() == null) {
                throw new DisguiseParseException(ChatColor.RED + "Error! This version of minecraft does not have that disguise!");
            }
            if (!map.containsKey(disguiseType)) {
                throw new DisguiseParseException(ChatColor.RED + "You are forbidden to use this disguise.");
            }
            optionPermissions = map.get(disguiseType);
            HashMap<String, Boolean> disguiseOptions = this.getDisguisePermission(sender, disguiseType);
            if (disguiseType.isPlayer()) {
                // If he is doing a player disguise
                if (args.length == 1) {
                    // He needs to give the player name
                    throw new DisguiseParseException(ChatColor.RED + "Error! You need to give a player name!");
                } else {
                    if (!disguiseOptions.isEmpty()
                            && (!disguiseOptions.containsKey(args[1].toLowerCase()) || !disguiseOptions
                            .get(args[1].toLowerCase()))) {
                        throw new DisguiseParseException(ChatColor.RED + "Error! You don't have permission to use that name!");
                    }
                    args[1] = args[1].replace("\\_", " ");
                    // Construct the player disguise
                    disguise = new PlayerDisguise(ChatColor.translateAlternateColorCodes('&', args[1]));
                    toSkip++;
                }
            } else {
                if (disguiseType.isMob()) { // Its a mob, use the mob constructor
                    boolean adult = true;
                    if (args.length > 1) {
                        if (args[1].equalsIgnoreCase("baby") || args[1].equalsIgnoreCase("adult")) {
                            usedOptions.add("setbaby");
                            doCheck(optionPermissions, usedOptions);
                            adult = args[1].equalsIgnoreCase("adult");
                            toSkip++;
                        }
                    }
                    disguise = new MobDisguise(disguiseType, adult);
                } else if (disguiseType.isMisc()) {
                    // Its a misc, we are going to use the MiscDisguise constructor.
                    int miscId = -1;
                    int miscData = -1;
                    String secondArg = null;
                    if (args.length > 1) {
                        // They have defined more arguements!
                        // If the first arg is a number
                        if (args[1].contains(":")) {
                            String[] split = args[1].split(":");
                            if (isNumeric(split[1])) {
                                secondArg = split[1];
                            }
                            args[1] = split[0];
                        }
                        if (isNumeric(args[1])) {
                            miscId = Integer.parseInt(args[1]);
                        } else {
                            if (disguiseType == DisguiseType.FALLING_BLOCK || disguiseType == DisguiseType.DROPPED_ITEM) {
                                for (Material mat : Material.values()) {
                                    if (mat.name().replace("_", "").equalsIgnoreCase(args[1].replace("_", ""))) {
                                        miscId = mat.getId();
                                        break;
                                    }
                                }
                            }
                        }
                        if (miscId != -1) {
                            switch (disguiseType) {
                                case PAINTING:
                                case FALLING_BLOCK:
                                case SPLASH_POTION:
                                case DROPPED_ITEM:
                                case FISHING_HOOK:
                                case ARROW:
                                case SMALL_FIREBALL:
                                case FIREBALL:
                                case WITHER_SKULL:
                                    break;
                                default:
                                    throw new DisguiseParseException(ChatColor.RED + "Error! " + disguiseType.toReadable()
                                            + " doesn't know what to do with " + args[1] + "!");
                            }
                            toSkip++;
                            // If they also defined a data value
                            if (args.length > 2 && secondArg == null && isNumeric(args[2])) {
                                secondArg = args[2];
                                toSkip++;
                            }
                            if (secondArg != null) {
                                if (disguiseType != DisguiseType.FALLING_BLOCK && disguiseType != DisguiseType.DROPPED_ITEM) {
                                    throw new DisguiseParseException(ChatColor.RED + "Error! Only the disguises "
                                            + DisguiseType.FALLING_BLOCK.toReadable() + " and "
                                            + DisguiseType.DROPPED_ITEM.toReadable() + " uses a second number!");
                                }
                                miscData = Integer.parseInt(secondArg);
                            }
                        }
                    }
                    if (!disguiseOptions.isEmpty() && miscId != -1) {
                        String toCheck = "" + miscId;
                        if (miscData == 0 || miscData == -1) {
                            if (!disguiseOptions.containsKey(toCheck) || !disguiseOptions.get(toCheck)) {
                                toCheck += ":0";
                            }
                        } else {
                            toCheck += ":" + miscData;
                        }
                        if (!disguiseOptions.containsKey(toCheck) || !disguiseOptions.get(toCheck)) {
                            throw new DisguiseParseException(ChatColor.RED
                                    + "Error! You do not have permission to use the parameter " + toCheck + " on the "
                                    + disguiseType.toReadable() + " disguise!");
                        }
                    }
                    if (miscId != -1) {
                        if (disguiseType == DisguiseType.FALLING_BLOCK) {
                            usedOptions.add("setblock");
                            doCheck(optionPermissions, usedOptions);
                        } else if (disguiseType == DisguiseType.PAINTING) {
                            usedOptions.add("setpainting");
                            doCheck(optionPermissions, usedOptions);
                        } else if (disguiseType == DisguiseType.SPLASH_POTION) {
                            usedOptions.add("setpotionid");
                            doCheck(optionPermissions, usedOptions);
                        }
                    }
                    // Construct the disguise
                    disguise = new MiscDisguise(disguiseType, miscId, miscData);
                }
            }
        }
        // Copy strings to their new range
        String[] newArgs = new String[args.length - toSkip];
        System.arraycopy(args, toSkip, newArgs, 0, args.length - toSkip);
        args = newArgs;
        Method[] methods = this.getDisguiseWatcherMethods(disguise.getWatcher().getClass());
        for (int i = 0; i < args.length; i += 2) {
            String methodName = args[i];
            String valueString = (args.length - 1 == i ? null : args[i + 1]);
            Method methodToUse = null;
            Object value = null;
            DisguiseParseException storedEx = null;
            int c = 0;
            while (c < methods.length) {
                try {
                    Entry<Method, Integer> entry = getMethod(methods, methodName, c);
                    if (entry == null) {
                        break;
                    }
                    methodToUse = entry.getKey();
                    c = entry.getValue();
                    methodName = methodToUse.getName();
                    Class<?>[] types = methodToUse.getParameterTypes();
                    Class param = types[0];
                    if (valueString != null) {
                        if (int.class == param) {
                            // Parse to integer
                            if (isNumeric(valueString)) {
                                value = Integer.parseInt(valueString);
                            } else {
                                throw parseToException("number", valueString, methodName);
                            }
                        } else if (float.class == param || double.class == param) {
                            // Parse to number
                            if (isDouble(valueString)) {
                                float obj = Float.parseFloat(valueString);
                                if (param == float.class) {
                                    value = obj;
                                } else if (param == double.class) {
                                    value = (double) obj;
                                }
                            } else {
                                throw parseToException("number.0", valueString, methodName);
                            }
                        } else if (param == String.class) {
                            // Parse to string
                            value = ChatColor.translateAlternateColorCodes('&', valueString);
                        } else if (param == AnimalColor.class) {
                            // Parse to animal color
                            try {
                                value = AnimalColor.valueOf(valueString.toUpperCase());
                            } catch (Exception ex) {
                                throw parseToException("animal color", valueString, methodName);
                            }
                        } else if (param == ItemStack.class) {
                            // Parse to itemstack
                            try {
                                value = parseToItemstack(valueString);
                            } catch (Exception ex) {
                                throw new DisguiseParseException(String.format(ex.getMessage(), methodName));
                            }
                        } else if (param == ItemStack[].class) {
                            // Parse to itemstack array
                            ItemStack[] items = new ItemStack[4];
                            String[] split = valueString.split(",");
                            if (split.length == 4) {
                                for (int a = 0; a < 4; a++) {
                                    try {
                                        items[a] = parseToItemstack(split[a]);
                                    } catch (Exception ex) {
                                        throw parseToException("item ID,ID,ID,ID" + ChatColor.RED + " or " + ChatColor.GREEN
                                                + "ID:Data,ID:Data,ID:Data,ID:Data combo", valueString, methodName);
                                    }
                                }
                            } else {
                                throw parseToException("item ID,ID,ID,ID" + ChatColor.RED + " or " + ChatColor.GREEN
                                        + "ID:Data,ID:Data,ID:Data,ID:Data combo", valueString, methodName);
                            }
                            value = items;
                        } else if (param.getSimpleName().equals("Color")) {
                            // Parse to horse color
                            value = callValueOf(param, valueString, methodName, "a horse color");
                        } else if (param.getSimpleName().equals("Style")) {
                            // Parse to horse style
                            value = callValueOf(param, valueString, methodName, "a horse style");
                        } else if (param.getSimpleName().equals("Profession")) {
                            // Parse to villager profession
                            value = callValueOf(param, valueString, methodName, "a villager profession");
                        } else if (param.getSimpleName().equals("Art")) {
                            // Parse to art type
                            value = callValueOf(param, valueString, methodName, "a painting art");
                        } else if (param.getSimpleName().equals("Type")) {
                            // Parse to ocelot type
                            value = callValueOf(param, valueString, methodName, "a ocelot type");
                        } else if (param == PotionEffectType.class) {
                            // Parse to potion effect
                            try {
                                PotionEffectType potionType = PotionEffectType.getByName(valueString.toUpperCase());
                                if (potionType == null && isNumeric(valueString)) {
                                    potionType = PotionEffectType.getById(Integer.parseInt(valueString));
                                }
                                if (potionType == null) {
                                    throw new DisguiseParseException();
                                }
                                value = potionType;
                            } catch (Exception ex) {
                                throw parseToException("a potioneffect type", valueString, methodName);
                            }
                        } else if (param == int[].class) {
                            String[] split = valueString.split(",");
                            int[] values = new int[split.length];
                            for (int b = 0; b < values.length; b++) {
                                try {
                                    values[b] = Integer.parseInt(split[b]);
                                } catch (NumberFormatException ex) {
                                    throw parseToException("Number,Number,Number...", valueString, methodName);
                                }
                            }
                            value = values;
                        } else if (param == BlockFace.class) {
                            try {
                                BlockFace face = BlockFace.valueOf(valueString.toUpperCase());
                                if (face.ordinal() > 4) {
                                    throw new DisguiseParseException();
                                }
                                value = face;
                            } catch (Exception ex) {
                                throw parseToException("a direction (north, east, south, west, up)", valueString, methodName);
                            }
                        } else if (param == RabbitType.class) {
                            try {
                                for (RabbitType type : RabbitType.values()) {
                                    if (type.name().replace("_", "")
                                            .equalsIgnoreCase(valueString.replace("_", "").replace(" ", ""))) {
                                        value = type;
                                        break;
                                    }
                                }
                                if (value == null) {
                                    throw new Exception();
                                }
                            } catch (Exception ex) {
                                throw parseToException("rabbit type (white, brown, patches...)", valueString, methodName);
                            }
                        }
                    }
                    if (value == null && boolean.class == param) {
                        if (valueString == null) {
                            value = true;
                            i--;
                        } else if (valueString.equalsIgnoreCase("true")) {
                            value = true;
                        } else if (valueString.equalsIgnoreCase("false")) {
                            value = false;
                        } else {
                            if (getMethod(methods, valueString, 0) == null) {
                                throw parseToException("true/false", valueString, methodName);
                            } else {
                                value = true;
                                i--;
                            }
                        }
                    }
                    if (value != null) {
                        break;
                    }
                } catch (DisguiseParseException ex) {
                    storedEx = ex;
                    methodToUse = null;
                } catch (Exception ex) {
                    ex.printStackTrace(System.out);
                    methodToUse = null;
                }
            }
            if (methodToUse == null) {
                if (storedEx != null) {
                    throw storedEx;
                }
                throw new DisguiseParseException(ChatColor.RED + "Cannot find the option " + methodName);
            }
            if (value == null) {
                throw new DisguiseParseException(ChatColor.RED + "No value was given for the option " + methodName);
            }
            if (!usedOptions.contains(methodName.toLowerCase())) {
                usedOptions.add(methodName.toLowerCase());
            }
            doCheck(optionPermissions, usedOptions);
            if (FlagWatcher.class.isAssignableFrom(methodToUse.getDeclaringClass())) {
                methodToUse.invoke(disguise.getWatcher(), value);
            } else {
                methodToUse.invoke(disguise, value);
            }
        }
        // Alright. We've constructed our disguise.
        return disguise;
    }

    private Entry<Method, Integer> getMethod(Method[] methods, String methodName, int toStart) {
        for (int i = toStart; i < methods.length; i++) {
            Method method = methods[i];
            if (!method.getName().startsWith("get") && method.getName().equalsIgnoreCase(methodName)
                    && method.getAnnotation(Deprecated.class) == null && method.getParameterTypes().length == 1) {
                return new HashMap.SimpleEntry(method, ++i);
            }
        }
        return null;
    }

    private Object callValueOf(Class<?> param, String valueString, String methodName, String description)
            throws DisguiseParseException {
        Object value;
        try {
            value = param.getMethod("valueOf", String.class).invoke(null, valueString.toUpperCase());
        } catch (Exception ex) {
            throw parseToException(description, valueString, methodName);
        }
        return value;
    }

    private boolean passesCheck(HashMap<ArrayList<String>, Boolean> map1, ArrayList<String> usedOptions) {
        boolean hasPermission = false;
        for (ArrayList<String> list : map1.keySet()) {
            boolean myPerms = true;
            for (String option : usedOptions) {
                if (!(map1.get(list) && list.contains("*")) && (list.contains(option) != map1.get(list))) {
                    myPerms = false;
                    break;
                }
            }
            if (myPerms) {
                hasPermission = true;
            }
        }
        return hasPermission;
    }

    private void doCheck(HashMap<ArrayList<String>, Boolean> optionPermissions, ArrayList<String> usedOptions)
            throws DisguiseParseException {
        if (!passesCheck(optionPermissions, usedOptions)) {
            throw new DisguiseParseException(ChatColor.RED + "You do not have the permission to use the option "
                    + usedOptions.get(usedOptions.size() - 1));
        }
    }

    private DisguiseParseException parseToException(String expectedValue, String receivedInstead, String methodName) {
        return new DisguiseParseException(ChatColor.RED + "Expected " + ChatColor.GREEN + expectedValue + ChatColor.RED
                + ", received " + ChatColor.GREEN + receivedInstead + ChatColor.RED + " instead for " + ChatColor.GREEN
                + methodName);
    }

    private ItemStack parseToItemstack(String string) throws Exception {
        String[] split = string.split(":", -1);
        if (isNumeric(split[0])) {
            int itemId = Integer.parseInt(split[0]);
            short itemDura = 0;
            if (split.length > 1) {
                if (isNumeric(split[1])) {
                    itemDura = Short.parseShort(split[1]);
                } else {
                    throw parseToException("item ID:Durability combo", string, "%s");
                }
            }
            return new ItemStack(itemId, 1, itemDura);
        } else {
            if (split.length == 1) {
                throw parseToException("item ID", string, "%s");
            } else {
                throw parseToException("item ID:Durability combo", string, "%s");
            }
        }
    }

    protected abstract void sendCommandUsage(CommandSender sender, HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> map);
}
