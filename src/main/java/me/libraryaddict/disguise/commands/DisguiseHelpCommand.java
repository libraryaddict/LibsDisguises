package me.libraryaddict.disguise.commands;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.RabbitType;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.BaseDisguiseCommand;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class DisguiseHelpCommand extends BaseDisguiseCommand {

    private class EnumHelp {

        private String enumDescription;
        private String enumName;
        private String[] enums;
        private String readableEnum;

        public EnumHelp(String enumName, String enumReadable, String enumDescription, Enum[] enums) {
            String[] strings = new String[enums.length];
            for (int i = 0; i < strings.length; i++) {
                strings[i] = toReadable(enums[i].name());
            }
            this.enumName = enumName;
            this.enumDescription = enumDescription;
            this.enums = strings;
            this.readableEnum = enumReadable;
        }

        public EnumHelp(String enumName, String enumReadable, String enumDescription, String[] enums) {
            this.enumName = enumName;
            this.enumDescription = enumDescription;
            this.enums = enums;
            this.readableEnum = enumReadable;
        }

        public String getEnumDescription() {
            return enumDescription;
        }

        public String getEnumName() {
            return enumName;
        }

        public String[] getEnums() {
            return enums;
        }

        public String getReadableEnum() {
            return readableEnum;
        }
    }

    private ArrayList<EnumHelp> enumHelp = new ArrayList<>();

    public DisguiseHelpCommand() {
        try {
            enumHelp.add(new EnumHelp("AnimalColor", "Animal colors", ChatColor.RED + "/disguisehelp AnimalColors "
                    + ChatColor.GREEN + "- View all the colors you can use for a animal color", AnimalColor.values()));
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        try {
            enumHelp.add(new EnumHelp("Art", "Arts", ChatColor.RED + "/disguisehelp Art " + ChatColor.GREEN
                    + "- View all the painting arts you can use on a painting disguise", (Enum[]) Class.forName("org.bukkit.Art")
                    .getEnumConstants()));
        } catch (Exception ex) {
        }
        try {
            enumHelp.add(new EnumHelp("HorseColor", "Horse colors", ChatColor.RED + "/disguisehelp HorseColors "
                    + ChatColor.GREEN + "- View all the colors you can use for a horses color", (Enum[]) Class.forName(
                            "org.bukkit.entity.Horse$Color").getEnumConstants()));
        } catch (Exception ex) {
        }
        try {
            enumHelp.add(new EnumHelp("HorseStyle", "Horse styles", ChatColor.RED + "/disguisehelp HorseStyles "
                    + ChatColor.GREEN + "- View all the styles you can use for a horses style", (Enum[]) Class.forName(
                            "org.bukkit.entity.Horse$Style").getEnumConstants()));
        } catch (Exception ex) {
        }
        try {
            enumHelp.add(new EnumHelp("OcelotType", "Ocelot types", ChatColor.RED + "/disguisehelp OcelotTypes "
                    + ChatColor.GREEN + "- View all the ocelot types you can use for ocelots", (Enum[]) Class.forName(
                            "org.bukkit.entity.Ocelot$Type").getEnumConstants()));
        } catch (Exception ex) {
        }
        try {
            ArrayList<String> enumReturns = new ArrayList<>();
            for (PotionEffectType potionType : PotionEffectType.values()) {
                if (potionType != null) {
                    enumReturns.add(toReadable(potionType.getName()) + ChatColor.RED + "(" + ChatColor.GREEN + potionType.getId()
                            + ChatColor.RED + ")");
                }
            }
            enumHelp.add(new EnumHelp("PotionEffect", "PotionEffect", ChatColor.RED + "/disguisehelp PotionEffect "
                    + ChatColor.GREEN + "- View all the potion effects you can set", enumReturns.toArray(new String[enumReturns
                            .size()])));
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        try {
            enumHelp.add(new EnumHelp("Profession", "Villager professions", ChatColor.RED + "/disguisehelp Professions "
                    + ChatColor.GREEN + "- View all the professions you can set on a villager", (Enum[]) Class.forName(
                            "org.bukkit.entity.Villager$Profession").getEnumConstants()));
        } catch (Exception ex) {
        }
        enumHelp.add(new EnumHelp("Direction", "Directions", ChatColor.RED + "/disguisehelp Directions " + ChatColor.GREEN
                + "- View the five directions usable on player setsleeping disguise", Arrays.copyOf(BlockFace.values(), 5)));
        enumHelp.add(new EnumHelp("RabbitType", "RabbitType", ChatColor.RED + "/disguisehelp RabbitType " + ChatColor.GREEN
                + "View the kinds of rabbits you can turn into", RabbitType.values()));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        for (String node : new String[]{"disguise", "disguiseradius", "disguiseentity", "disguiseplayer"}) {
            HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> permMap = getPermissions(sender, "libsdisguises." + node
                    + ".");
            if (!permMap.isEmpty()) {
                if (args.length == 0) {
                    sendCommandUsage(sender, null);
                    return true;
                } else {
                    EnumHelp help = null;
                    for (EnumHelp s : enumHelp) {
                        if (args[0].equalsIgnoreCase(s.getEnumName()) || args[0].equalsIgnoreCase(s.getEnumName() + "s")) {
                            help = s;
                            break;
                        }
                    }
                    if (help != null) {
                        sender.sendMessage(ChatColor.RED + help.getReadableEnum() + ": " + ChatColor.GREEN
                                + StringUtils.join(help.getEnums(), ChatColor.RED + ", " + ChatColor.GREEN));
                        return true;
                    }
                    DisguiseType type = null;
                    for (DisguiseType disguiseType : DisguiseType.values()) {
                        if (args[0].equalsIgnoreCase(disguiseType.name())
                                || disguiseType.name().replace("_", "").equalsIgnoreCase(args[0])) {
                            type = disguiseType;
                            break;
                        }
                    }
                    if (type == null) {
                        sender.sendMessage(ChatColor.RED + "Cannot find the disguise " + args[0]);
                        return true;
                    }
                    if (!permMap.containsKey(type)) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission for that disguise!");
                        return true;
                    }
                    ArrayList<String> methods = new ArrayList<>();
                    HashMap<String, ChatColor> map = new HashMap<>();
                    Class watcher = type.getWatcherClass();
                    int ignored = 0;
                    try {
                        for (Method method : this.getDisguiseWatcherMethods(watcher)) {
                            if (!method.getName().startsWith("get") && method.getParameterTypes().length == 1
                                    && method.getAnnotation(Deprecated.class) == null) {
                                if (args.length < 2 || !args[1].equalsIgnoreCase("show")) {
                                    boolean allowed = false;
                                    for (ArrayList<String> key : permMap.get(type).keySet()) {
                                        if (permMap.get(type).get(key)) {
                                            if (key.contains("*") || key.contains(method.getName().toLowerCase())) {
                                                allowed = true;
                                                break;
                                            }
                                        } else if (!key.contains(method.getName().toLowerCase())) {
                                            allowed = true;
                                            break;
                                        }
                                    }
                                    if (!allowed) {
                                        ignored++;
                                        continue;
                                    }
                                }
                                Class c = method.getParameterTypes()[0];
                                String valueType = null;
                                if (c == String.class) {
                                    valueType = "String";
                                } else if (boolean.class == c) {
                                    valueType = "True/False";
                                } else if (int.class == c) {
                                    valueType = "Number";
                                } else if (float.class == c || double.class == c) {
                                    valueType = "Decimal";
                                } else if (AnimalColor.class == c) {
                                    valueType = "Color";
                                } else if (ItemStack.class == c) {
                                    valueType = "Item (id:damage)";
                                } else if (ItemStack[].class == c) {
                                    valueType = "4 items (id:damage,id,...)";
                                } else if (c.getSimpleName().equals("Style")) {
                                    valueType = "Horse Style";
                                } else if (c.getSimpleName().equals("Color")) {
                                    valueType = "Horse Color";
                                } else if (c.getSimpleName().equals("Type")) {
                                    valueType = "Ocelot type";
                                } else if (c.getSimpleName().equals("Profession")) {
                                    valueType = "Villager Profession";
                                } else if (PotionEffectType.class == c) {
                                    valueType = "Potion effect";
                                } else if (c == int[].class) {
                                    valueType = "number,number,number...";
                                } else if (c == BlockFace.class) {
                                    valueType = "direction";
                                } else if (c == RabbitType.class) {
                                    valueType = "rabbit type";
                                }
                                if (valueType != null) {
                                    ChatColor methodColor = ChatColor.YELLOW;
                                    Class<?> declaring = method.getDeclaringClass();
                                    if (declaring == LivingWatcher.class) {
                                        methodColor = ChatColor.AQUA;
                                    } else if (!(FlagWatcher.class.isAssignableFrom(declaring)) || declaring == FlagWatcher.class) {
                                        methodColor = ChatColor.GRAY;
                                    }
                                    String str = method.getName() + ChatColor.DARK_RED + "(" + ChatColor.GREEN + valueType
                                            + ChatColor.DARK_RED + ")";
                                    map.put(str, methodColor);
                                    methods.add(str);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                    Collections.sort(methods, String.CASE_INSENSITIVE_ORDER);
                    for (int i = 0; i < methods.size(); i++) {
                        methods.set(i, map.get(methods.get(i)) + methods.get(i));
                    }
                    if (methods.isEmpty()) {
                        methods.add(ChatColor.RED + "No options with permission to use");
                    }
                    sender.sendMessage(ChatColor.DARK_RED + type.toReadable() + " options: "
                            + StringUtils.join(methods, ChatColor.DARK_RED + ", "));
                    if (ignored > 0) {
                        sender.sendMessage(ChatColor.RED + "Ignored " + ignored
                                + " options you do not have permission to view. Add 'show' to view unusable options.");
                    }
                    return true;
                }
            }
        }
        sender.sendMessage(ChatColor.RED + "You are forbidden to use this command.");
        return true;
    }

    /**
     * Send the player the information
     */
    @Override
    protected void sendCommandUsage(CommandSender sender, HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> map) {
        sender.sendMessage(ChatColor.RED
                + "/disguisehelp <DisguiseType> "
                + ChatColor.GREEN
                + "- View the options you can set on a disguise. Add 'show' to reveal the options you don't have permission to use");
        for (EnumHelp s : enumHelp) {
            sender.sendMessage(s.getEnumDescription());
        }
    }

    public String toReadable(String string) {
        String[] split = string.split("_");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].substring(0, 1) + split[i].substring(1).toLowerCase();
        }
        return StringUtils.join(split, "_");
    }
}
