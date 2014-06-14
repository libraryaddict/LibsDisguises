package me.libraryaddict.disguise.commands;

import java.util.ArrayList;
import java.util.HashMap;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.DisguiseListener;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.BaseDisguiseCommand;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class DisguiseCloneCommand extends BaseDisguiseCommand {

    private DisguiseListener listener;

    public DisguiseCloneCommand(DisguiseListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage(ChatColor.RED + "You may not use this command from the console!");
            return true;
        }
        if (sender.hasPermission("libsdisguises.disguise.disguiseclone")) {
            boolean doEnquipment = true;
            boolean doSneak = false;
            boolean doSprint = false;
            for (String option : args) {
                if (StringUtils.startsWithIgnoreCase(option, "ignoreEnquip")) {
                    doEnquipment = false;
                } else if (option.equalsIgnoreCase("doSneakSprint")) {
                    doSneak = true;
                    doSprint = true;
                } else if (option.equalsIgnoreCase("doSneak")) {
                    doSneak = true;
                } else if (option.equalsIgnoreCase("doSprint")) {
                    doSprint = true;
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "Unknown option '" + option
                            + "' - Valid options are 'IgnoreEnquipment' 'DoSneakSprint' 'DoSneak' 'DoSprint'");
                    return true;
                }
            }
            listener.setDisguiseClone(sender.getName(), new Boolean[] { doEnquipment, doSneak, doSprint });
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
    protected void sendCommandUsage(CommandSender sender, HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> map) {
        sender.sendMessage(ChatColor.DARK_GREEN
                + "Right click a entity to get a disguise reference you can pass to other disguise commands!");
        sender.sendMessage(ChatColor.DARK_GREEN + "Beware however, the reference bypasses all permissions checks");
        sender.sendMessage(ChatColor.DARK_GREEN + "/disguiseclone IgnoreEnquipment" + ChatColor.DARK_GREEN + "("
                + ChatColor.GREEN + "Optional" + ChatColor.DARK_GREEN + ")");
    }
}
