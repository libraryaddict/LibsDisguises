package me.libraryaddict.disguise.commands;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import me.libraryaddict.disguise.BaseDisguiseCommand;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class DisguiseHelpCommand extends BaseDisguiseCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        for (String node : new String[] { "disguise", "disguiseradius", "disguiseentity", "disguiseplayer" }) {
            ArrayList<String> allowedDisguises = getAllowedDisguises(sender, node);
            if (!allowedDisguises.isEmpty()) {
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.RED + "/disguisehelp <Disguise>");
                    return true;
                    // sender.sendMessage(ChatColor.RED + "/disguisehelp <Disguise> <Option>");
                } else {
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
                                }
                                if (valueType != null) {
                                    methods.add(ChatColor.RED + method.getName() + ChatColor.DARK_RED + " (" + ChatColor.GREEN
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
    }
}
