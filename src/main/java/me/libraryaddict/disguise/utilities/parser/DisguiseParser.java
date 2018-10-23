package me.libraryaddict.disguise.utilities.parser;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsMsg;
import me.libraryaddict.disguise.utilities.TranslateType;
import me.libraryaddict.disguise.utilities.parser.params.ParamInfo;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisguiseParser {
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

                String beginning = "libsdisguises.options." + permNode + ".";

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

        for (Entry<String, Disguise> entry : DisguiseConfig.getCustomDisguises().entrySet()) {
            perms[i++] = new DisguisePerm(entry.getValue().getType(), entry.getKey());
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
     * Splits a string while respecting quotes
     */
    public static String[] split(String string) {
        Matcher matcher = Pattern.compile("\"(?:\"(?=\\S)|\\\\\"|[^\"])*(?:[^\\\\]\"(?=\\s|$))|\\S+").matcher(string);

        List<String> list = new ArrayList<>();

        while (matcher.find()) {
            list.add(matcher.group());
        }

        return list.toArray(new String[0]);
    }

    /**
     * Returns the disguise if it all parsed correctly. Returns a exception with a complete message if it didn't. The
     * commandsender is purely used for checking permissions. Would defeat the purpose otherwise. To reach this
     * point, the
     * disguise has been feed a proper disguisetype.
     */
    public static Disguise parseDisguise(CommandSender sender, String permNode, String[] args,
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
            Entry<String, Disguise> customDisguise = DisguiseConfig.getCustomDisguise(args[0]);

            if (customDisguise != null) {
                disguise = customDisguise.getValue().clone();
            }

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
                        if (!disguiseOptions.isEmpty() && (!disguiseOptions.containsKey(args[1].toLowerCase()) ||
                                !disguiseOptions.get(args[1].toLowerCase()))) {
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
                    int miscData = -1;
                    String secondArg = null;

                    if (args.length > 1) {
                        // They have defined more arguments!
                        // If the first arg is a number
                        if (args[1].contains(":")) {
                            String[] split = args[1].split(":");
                            if (isInteger(split[1])) {
                                secondArg = split[1];
                            }
                            args[1] = split[0];
                        }

                        if (isInteger(args[1])) {
                            miscId = Integer.parseInt(args[1]);
                        } else {
                            if (disguisePerm.getType() == DisguiseType.FALLING_BLOCK ||
                                    disguisePerm.getType() == DisguiseType.DROPPED_ITEM) {
                                for (Material mat : Material.values()) {
                                    if (mat.name().replace("_", "").equalsIgnoreCase(args[1].replace("_", ""))) {
                                        itemStack = new ItemStack(mat);
                                        miscId = mat.getId();
                                        break;
                                    }
                                }
                            }
                        }
                        if (miscId != -1) {
                            switch (disguisePerm.getType()) {
                                case PAINTING:
                                case FALLING_BLOCK:
                                case SPLASH_POTION:
                                case DROPPED_ITEM:
                                case FISHING_HOOK:
                                case ARROW:
                                case TIPPED_ARROW:
                                case SPECTRAL_ARROW:
                                case SMALL_FIREBALL:
                                case FIREBALL:
                                case WITHER_SKULL:
                                case TRIDENT:
                                    break;
                                default:
                                    throw new DisguiseParseException(LibsMsg.PARSE_TOO_MANY_ARGS,
                                            disguisePerm.toReadable(), args[1]);
                            }
                            toSkip++;
                            // If they also defined a data value
                            if (args.length > 2 && secondArg == null && isInteger(args[2])) {
                                secondArg = args[2];
                                toSkip++;
                            }
                            if (secondArg != null) {
                                if (disguisePerm.getType() != DisguiseType.FALLING_BLOCK &&
                                        disguisePerm.getType() != DisguiseType.DROPPED_ITEM) {
                                    throw new DisguiseParseException(LibsMsg.PARSE_USE_SECOND_NUM,
                                            DisguiseType.FALLING_BLOCK.toReadable(),
                                            DisguiseType.DROPPED_ITEM.toReadable());
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
                            throw new DisguiseParseException(LibsMsg.PARSE_NO_PERM_PARAM, toCheck,
                                    disguisePerm.toReadable());
                        }
                    }

                    if (miscId != -1) {
                        if (disguisePerm.getType() == DisguiseType.FALLING_BLOCK) {
                            usedOptions.add("setblock");

                            doCheck(sender, permissions, disguisePerm, usedOptions);
                        } else if (disguisePerm.getType() == DisguiseType.PAINTING) {
                            usedOptions.add("setpainting");

                            doCheck(sender, permissions, disguisePerm, usedOptions);
                        } else if (disguisePerm.getType() == DisguiseType.SPLASH_POTION) {
                            usedOptions.add("setpotionid");

                            doCheck(sender, permissions, disguisePerm, usedOptions);
                        }
                    }

                    // Construct the disguise
                    if (disguisePerm.getType() == DisguiseType.DROPPED_ITEM ||
                            disguisePerm.getType() == DisguiseType.FALLING_BLOCK) {
                        disguise = new MiscDisguise(disguisePerm.getType(), itemStack);
                    } else {
                        disguise = new MiscDisguise(disguisePerm.getType(), miscId, miscData);
                    }
                }
            }
        }

        // Copy strings to their new range
        String[] newArgs = new String[args.length - toSkip];
        System.arraycopy(args, toSkip, newArgs, 0, args.length - toSkip);

        callMethods(sender, disguise, permissions, disguisePerm, usedOptions, newArgs);

        // Alright. We've constructed our disguise.
        return disguise;
    }

    public static void callMethods(CommandSender sender, Disguise disguise, DisguisePermissions disguisePermission,
            DisguisePerm disguisePerm, Collection<String> usedOptions,
            String[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            DisguiseParseException {
        Method[] methods = ParamInfoManager.getDisguiseWatcherMethods(disguise.getWatcher().getClass());
        List<String> list = new ArrayList<>(Arrays.asList(args));

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

            doCheck(sender, disguisePermission, disguisePerm, usedOptions);

            if (FlagWatcher.class.isAssignableFrom(methodToUse.getDeclaringClass())) {
                methodToUse.invoke(disguise.getWatcher(), valueToSet);
            } else {
                methodToUse.invoke(disguise, valueToSet);
            }
        }
    }
}
