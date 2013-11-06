package me.libraryaddict.disguise.commands;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import me.libraryaddict.disguise.BaseDisguiseCommand;
import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Ocelot.Type;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class DisguiseHelpCommand extends BaseDisguiseCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        for (String node : new String[] { "disguise", "disguiseradius", "disguiseentity", "disguiseplayer" }) {
            ArrayList<String> allowedDisguises = getAllowedDisguises(sender, node);
            if (!allowedDisguises.isEmpty()) {
                if (args.length == 0) {
                    sendCommandUsage(sender);
                    return true;
                    // sender.sendMessage(ChatColor.RED + "/disguisehelp <Disguise> <Option>");
                } else {
                    Enum[] enums = null;
                    String enumName = null;
                    ArrayList<String> enumReturns = new ArrayList<String>();
                    if (args[0].equalsIgnoreCase("animalcolor") || args[0].equalsIgnoreCase("animalcolors")) {
                        enums = AnimalColor.values();
                        enumName = "Animal colors";
                    } else if (args[0].equalsIgnoreCase("horsecolor") || args[0].equalsIgnoreCase("horsecolors")) {
                        enums = Color.values();
                        enumName = "Horse colors";
                    } else if (args[0].equalsIgnoreCase("horsestyle") || args[0].equalsIgnoreCase("horsestyles")) {
                        enums = Style.values();
                        enumName = "Horse styles";
                    } else if (args[0].equalsIgnoreCase("OcelotType") || args[0].equalsIgnoreCase("OcelotTypes")) {
                        enums = Type.values();
                        enumName = "Ocelot types";
                    } else if (args[0].equalsIgnoreCase("Profession") || args[0].equalsIgnoreCase("Professions")) {
                        enums = Profession.values();
                        enumName = "Villager professions";
                    } else if (args[0].equalsIgnoreCase("PotionEffect") || args[0].equalsIgnoreCase("PotionEffects")) {
                        enumName = "Potioneffects";
                        for (PotionEffectType potionType : PotionEffectType.values()) {
                            if (potionType != null)
                                enumReturns.add(toReadable(potionType.getName()) + ChatColor.RED + "(" + ChatColor.GREEN
                                        + potionType.getId() + ChatColor.RED + ")");
                        }
                    }
                    if (enums != null) {
                        for (Enum enumType : enums) {
                            enumReturns.add(toReadable(enumType.name()));
                        }
                    }
                    if (!enumReturns.isEmpty()) {
                        sender.sendMessage(ChatColor.RED + enumName + ": " + ChatColor.GREEN
                                + StringUtils.join(enumReturns, ChatColor.RED + ", " + ChatColor.GREEN));
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
                    ArrayList<String> methods = new ArrayList<String>();
                    Class watcher = type.getWatcherClass();
                    try {
                        for (Method method : watcher.getMethods()) {
                            if (!method.getName().startsWith("get") && method.getParameterTypes().length == 1) {
                                Class c = method.getParameterTypes()[0];
                                String valueType = null;
                                if (c == String.class)
                                    valueType = "String";
                                else if (boolean.class == c)
                                    valueType = "True/False";
                                else if (float.class == c || double.class == c || int.class == c) {
                                    valueType = "Number";
                                } else if (AnimalColor.class == c) {
                                    valueType = "Color";
                                } else if (ItemStack.class == c) {
                                    valueType = "Item ID with optional :Durability";
                                } else if (ItemStack[].class == c) {
                                    valueType = "Item ID,ID,ID,ID with optional :Durability";
                                } else if (Style.class == c) {
                                    valueType = "Horse Style";
                                } else if (Color.class == c) {
                                    valueType = "Horse Color";
                                } else if (Type.class == c) {
                                    valueType = "Ocelot type";
                                } else if (Profession.class == c) {
                                    valueType = "Villager Profession";
                                } else if (PotionEffectType.class == c) {
                                    valueType = "Potioneffect";
                                }
                                if (valueType != null) {
                                    methods.add(ChatColor.RED + method.getName() + ChatColor.DARK_RED + "(" + ChatColor.GREEN
                                            + valueType + ChatColor.DARK_RED + ")");
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    Collections.sort(methods, String.CASE_INSENSITIVE_ORDER);
                    sender.sendMessage(ChatColor.DARK_RED + type.toReadable() + " options: "
                            + StringUtils.join(methods, ChatColor.DARK_RED + ", "));
                    return true;
                }
            }
        }
        return true;
    }

    /**
     * Send the player the information
     */
    protected void sendCommandUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "/disguisehelp <DisguiseType> " + ChatColor.GREEN
                + "- View the options you can set on a disguise");
        sender.sendMessage(ChatColor.RED + "/disguisehelp AnimalColors " + ChatColor.GREEN
                + "- View all the colors you can use for a animal color");
        sender.sendMessage(ChatColor.RED + "/disguisehelp HorseColors " + ChatColor.GREEN
                + "- View all the colors you can use for a horses color");
        sender.sendMessage(ChatColor.RED + "/disguisehelp HorseStyles " + ChatColor.GREEN
                + "- View all the styles you can use for a horses style");
        sender.sendMessage(ChatColor.RED + "/disguisehelp OcelotTypes " + ChatColor.GREEN
                + "- View all the ocelot types you can use for ocelots");
        sender.sendMessage(ChatColor.RED + "/disguisehelp Professions " + ChatColor.GREEN
                + "- View all the professions you can set on a villager");
        sender.sendMessage(ChatColor.RED + "/disguisehelp PotionEffect " + ChatColor.GREEN
                + "- View all the potion effects you can set");
    }

    public String toReadable(String string) {
        String[] split = string.split("_");
        for (int i = 0; i < split.length; i++)
            split[i] = split[i].substring(0, 1) + split[i].substring(1).toLowerCase();
        return StringUtils.join(split, "_");
    }
}
