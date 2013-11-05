package me.libraryaddict.disguise.commands;

import java.util.ArrayList;
import java.util.Collections;

import me.libraryaddict.disguise.BaseDisguiseCommand;
import me.libraryaddict.disguise.DisguiseListener;
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

public class DisguiseEntityCommand extends BaseDisguiseCommand {

    private DisguiseListener listener;

    public DisguiseEntityCommand(DisguiseListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage(ChatColor.RED + "You may not use this command from the console!");
            return true;
        }
        try {
            Disguise disguise = parseDisguise(sender, args);
            listener.setSlap(sender.getName(), disguise);
            sender.sendMessage(ChatColor.RED + "Right click a entity in the next 10 seconds to disguise it as a "
                    + toReadable(disguise.getType().name()) + "!");
        } catch (Exception ex) {
            if (ex.getMessage() != null) {
                sender.sendMessage(ex.getMessage());
            }
        }
        return true;
    }

    /**
     * Send the player the information
     */
    protected void sendCommandUsage(CommandSender sender) {
        ArrayList<String> allowedDisguises = getAllowedDisguises(sender, "disguiseentity");
        sender.sendMessage(ChatColor.DARK_GREEN + "Choose a disguise then slap a entity to disguise it!");
        sender.sendMessage(ChatColor.DARK_GREEN + "You can use the disguises: " + ChatColor.GREEN
                + StringUtils.join(allowedDisguises, ChatColor.RED + ", " + ChatColor.GREEN));
        if (allowedDisguises.contains("player"))
            sender.sendMessage(ChatColor.DARK_GREEN + "/disguiseentity player <Name>");
        sender.sendMessage(ChatColor.DARK_GREEN + "/disguise <DisguiseType> <Baby>");
        if (allowedDisguises.contains("dropped_item") || allowedDisguises.contains("falling_block"))
            sender.sendMessage(ChatColor.DARK_GREEN + "/disguiseentity <Dropped_Item/Falling_Block> <Id> <Durability>");
    }

    private String toReadable(String name) {
        String[] split = name.split("_");
        for (int i = 0; i < split.length; i++)
            split[i] = split[i].substring(0, 1) + split[i].substring(1).toLowerCase();
        return StringUtils.join(split, " ");
    }
}
