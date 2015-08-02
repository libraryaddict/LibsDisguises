package me.libraryaddict.disguise.commands;

import java.util.ArrayList;
import java.util.HashMap;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.DisguiseListener;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.BaseDisguiseCommand;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class DisguiseCloneCommand extends BaseDisguiseCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage(ChatColor.RED + "You may not use this command from the console!");
            return true;
        }
        if (sender.hasPermission("libsdisguises.disguise.disguiseclone")) {
            boolean doEquipment = true;
            boolean doSneak = false;
            boolean doSprint = false;
            for (String option : args) {
                if (StringUtils.startsWithIgnoreCase(option, "ignoreEquip")
                        || StringUtils.startsWithIgnoreCase(option, "ignoreEnquip")) {
                    doEquipment = false;
                } else if (option.equalsIgnoreCase("doSneakSprint")) {
                    doSneak = true;
                    doSprint = true;
                } else if (option.equalsIgnoreCase("doSneak")) {
                    doSneak = true;
                } else if (option.equalsIgnoreCase("doSprint")) {
                    doSprint = true;
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "Unknown option '" + option
                            + "' - Valid options are 'IgnoreEquipment' 'DoSneakSprint' 'DoSneak' 'DoSprint'");
                    return true;
                }
            }
            LibsDisguises.instance.getListener().setDisguiseClone(sender.getName(), new Boolean[]{doEquipment, doSneak, doSprint});
            sender.sendMessage(ChatColor.RED + "Right click a entity in the next " + DisguiseConfig.getDisguiseCloneExpire()
                    + " seconds to grab the disguise reference!");
        } else {
            sender.sendMessage(ChatColor.RED + "You are forbidden to use this command.");
        }
        return true;
    }

    /**
     * Send the player the information
     */
    @Override
    protected void sendCommandUsage(CommandSender sender, HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> map) {
        sender.sendMessage(ChatColor.DARK_GREEN
                + "Right click a entity to get a disguise reference you can pass to other disguise commands!");
        sender.sendMessage(ChatColor.DARK_GREEN
                + "Security note: Any references you create will be available to all players able to use disguise references.");
        sender.sendMessage(ChatColor.DARK_GREEN + "/disguiseclone IgnoreEquipment" + ChatColor.DARK_GREEN + "(" + ChatColor.GREEN
                + "Optional" + ChatColor.DARK_GREEN + ")");
    }
}
