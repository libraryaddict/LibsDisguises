package me.libraryaddict.disguise.commands;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import me.libraryaddict.disguise.BaseDisguiseCommand;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                    try {
                        type = DisguiseType.valueOf(args[0].toUpperCase());
                    } catch (Exception ex) {
                        sender.sendMessage(ChatColor.RED + "Cannot find the disguise " + args[0]);
                        return true;
                    }
                    if (type != null) {
                        ArrayList<String> methods = new ArrayList<String>();
                        Class watcher = type.getWatcherClass();
                        try {
                            for (Method method : watcher.getMethods()) {
                                if (method.getParameterTypes().length == 1) {
                                    Class c = method.getParameterTypes()[0];
                                    String valueType = null;
                                    if (c == String.class)
                                        valueType = "String";
                                    else if (c.isAssignableFrom(Boolean.class))
                                        valueType = "True/False";
                                    else if (c.isAssignableFrom(Float.class) || c.isAssignableFrom(Double.class)
                                            || c.isAssignableFrom(Integer.class)) {
                                        valueType = "Number";
                                    }
                                    if (valueType != null) {
                                        methods.add(ChatColor.RED + method.getName() + " (" + ChatColor.GREEN + valueType
                                                + ChatColor.RED + ")");
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        sender.sendMessage(ChatColor.DARK_RED + "Options: " + StringUtils.join(methods, ", "));
                        return true;
                    }
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
