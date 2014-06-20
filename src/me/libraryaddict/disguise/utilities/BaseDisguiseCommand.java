package me.libraryaddict.disguise.utilities;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.potion.PotionEffectType;

public abstract class BaseDisguiseCommand implements CommandExecutor {

    protected ArrayList<String> getAllowedDisguises(HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> hashMap) {
        ArrayList<String> allowedDisguises = new ArrayList<String>();
        for (DisguiseType type : hashMap.keySet()) {
            allowedDisguises.add(type.toReadable().replace(" ", "_"));
        }
        Collections.sort(allowedDisguises, String.CASE_INSENSITIVE_ORDER);
        return allowedDisguises;
    }

    protected HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> getPermissions(CommandSender sender) {
        return getPermissions(sender, "libsdisguises." + getClass().getSimpleName().replace("Command", "").toLowerCase() + ".");
    }

    /**
     * Get perms for the node. Returns a hashmap of allowed disguisetypes and their options
     */
    protected HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> getPermissions(CommandSender sender,
            String permissionNode) {

        HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> singleDisguises = new HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>>();
        HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> rangeDisguises = new HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>>();
        HashMap<String, Boolean> perms = new HashMap<String, Boolean>();

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
                try {
                    DisguiseType type = DisguiseType.valueOf(disguiseType.toUpperCase());
                    HashMap<ArrayList<String>, Boolean> list;
                    if (singleDisguises.containsKey(type)) {
                        list = singleDisguises.get(type);
                    } else {
                        list = new HashMap<ArrayList<String>, Boolean>();
                        singleDisguises.put(type, list);
                    }
                    HashMap<ArrayList<String>, Boolean> map1 = getOptions(perm);
                    list.put(map1.keySet().iterator().next(), map1.values().iterator().next());
                } catch (Exception ex) {
                    for (DisguiseType type : DisguiseType.values()) {
                        HashMap<ArrayList<String>, Boolean> options = null;
                        Class entityClass = type.getEntityType() == null ? Entity.class : type.getEntityType().getEntityClass();
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
                                list = new HashMap<ArrayList<String>, Boolean>();
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
                try {
                    DisguiseType type = DisguiseType.valueOf(disguiseType.toUpperCase());
                    singleDisguises.remove(type);
                } catch (Exception ex) {
                    for (DisguiseType type : DisguiseType.values()) {
                        boolean foundHim = false;
                        Class entityClass = type.getEntityType() == null ? Entity.class : type.getEntityType().getEntityClass();
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
        HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> map = new HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>>();
        for (DisguiseType type : DisguiseType.values()) {
            HashMap<ArrayList<String>, Boolean> temp = new HashMap<ArrayList<String>, Boolean>();
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
        ArrayList<String> list = new ArrayList<String>();
        boolean isRemove = true;
        String[] split = perm.split("\\.");
        for (int i = 1; i < split.length; i++) {
            String option = split[i];
            boolean value = option.startsWith("-");
            if (value) {
                option = option.substring(1);
                isRemove = false;
            }
            if (option.equals("baby"))
                option = "setbaby";
            list.add(option);
        }
        HashMap<ArrayList<String>, Boolean> options = new HashMap<ArrayList<String>, Boolean>();
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
     * Returns the disguise if it all parsed correctly. Returns a exception with a complete message if it didn't. The
     * commandsender is purely used for checking permissions. Would defeat the purpose otherwise. To reach this point, the
     * disguise has been feed a proper disguisetype.
     */
    protected Disguise parseDisguise(CommandSender sender, String[] args,
            HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> map) throws Exception {
        if (map.isEmpty()) {
            throw new Exception(ChatColor.RED + "You are forbidden to use this command.");
        }
        if (args.length == 0) {
            sendCommandUsage(sender, map);
            throw new Exception();
        }
        // How many args to skip due to the disugise being constructed
        // Time to start constructing the disguise.
        // We will need to check between all 3 kinds of disguises
        int toSkip = 1;
        ArrayList<String> usedOptions = new ArrayList<String>();
        Disguise disguise = null;
        HashMap<ArrayList<String>, Boolean> optionPermissions;
        if (args[0].startsWith("@")) {
            if (sender.hasPermission("libsdisguises.disguise.disguiseclone")) {
                disguise = DisguiseUtilities.getClonedDisguise(args[0].toLowerCase());
                if (disguise == null) {
                    throw new Exception(ChatColor.RED + "Cannot find a disguise under the reference " + args[0]);
                }
            } else {
                throw new Exception(ChatColor.RED + "You do not have perimssion to use disguise references!");
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
                throw new Exception(ChatColor.RED + "Error! The disguise " + ChatColor.GREEN + args[0] + ChatColor.RED
                        + " doesn't exist!");
            }
            if (disguiseType.getEntityType() == null) {
                throw new Exception(ChatColor.RED + "Error! This version of minecraft does not have that disguise!");
            }
            if (!map.containsKey(disguiseType)) {
                throw new Exception(ChatColor.RED + "You are forbidden to use this disguise.");
            }
            optionPermissions = map.get(disguiseType);
            if (disguiseType.isPlayer()) {// If he is doing a player disguise
                if (args.length == 1) {
                    // He needs to give the player name
                    throw new Exception(ChatColor.RED + "Error! You need to give a player name!");
                } else {
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
                    if (args.length > 1) {
                        // They have defined more arguements!
                        // If the first arg is a number
                        if (isNumeric(args[1])) {
                            miscId = Integer.parseInt(args[1]);
                            toSkip++;
                            // If they also defined a data value
                            if (args.length > 2) {
                                if (isNumeric(args[2])) {
                                    miscData = Integer.parseInt(args[2]);
                                    toSkip++;
                                }
                            }
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
        for (int i = 0; i < args.length; i += 2) {
            String methodName = args[i];
            String valueString = (args.length - 1 == i ? null : args[i + 1]);
            Method methodToUse = null;
            Object value = null;
            for (Method method : disguise.getWatcher().getClass().getMethods()) {
                if (!method.getName().startsWith("get") && method.getName().equalsIgnoreCase(methodName)
                        && method.getAnnotation(Deprecated.class) == null && method.getParameterTypes().length == 1) {
                    methodToUse = method;
                    break;
                }
            }
            if (methodToUse == null) {
                throw new Exception(ChatColor.RED + "Cannot find the option " + methodName);
            }
            methodName = methodToUse.getName();
            Class<?>[] types = methodToUse.getParameterTypes();
            if (types.length == 1) {
                Class param = types[0];
                if (boolean.class == param) {
                    // Parse to boolean
                    if (valueString == null || !("true".equalsIgnoreCase(valueString) || "false".equalsIgnoreCase(valueString))) {
                        value = true;
                        i--;
                    } else {
                        value = "true".equalsIgnoreCase(valueString);
                    }
                } else {
                    if (valueString == null) {
                        throw new Exception(ChatColor.RED + "No value was given for the option " + methodName);
                    }
                    if (int.class == param) {
                        // Parse to integer
                        if (isNumeric(valueString)) {
                            value = (int) Integer.parseInt(valueString);
                        } else {
                            throw parseToException("number", valueString, methodName);
                        }
                    } else if (float.class == param || double.class == param) {
                        // Parse to number
                        if (isDouble(valueString)) {
                            float obj = Float.parseFloat(valueString);
                            if (param == float.class) {
                                value = (float) obj;
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
                            throw new Exception(String.format(ex.getMessage(), methodName));
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
                            if (potionType == null)
                                throw new Exception();
                            value = potionType;
                        } catch (Exception ex) {
                            throw parseToException("a potioneffect type", valueString, methodName);
                        }
                    }
                }
            }
            if (!usedOptions.contains(methodName.toLowerCase())) {
                usedOptions.add(methodName.toLowerCase());
            }
            doCheck(optionPermissions, usedOptions);
            methodToUse.invoke(disguise.getWatcher(), value);
        }
        // Alright. We've constructed our disguise.
        return disguise;
    }

    private Object callValueOf(Class<?> param, String valueString, String methodName, String description) throws Exception {
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

    private void doCheck(HashMap<ArrayList<String>, Boolean> optionPermissions, ArrayList<String> usedOptions) throws Exception {
        if (!passesCheck(optionPermissions, usedOptions)) {
            throw new Exception(ChatColor.RED + "You do not have the permission to use the option "
                    + usedOptions.get(usedOptions.size() - 1));
        }
    }

    private Exception parseToException(String expectedValue, String receivedInstead, String methodName) {
        return new Exception(ChatColor.RED + "Expected " + ChatColor.GREEN + expectedValue + ChatColor.RED + ", received "
                + ChatColor.GREEN + receivedInstead + ChatColor.RED + " instead for " + ChatColor.GREEN + methodName);
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
