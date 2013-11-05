package me.libraryaddict.disguise.commands;

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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class DisguiseRadiusCommand extends BaseDisguiseCommand {
    private int maxRadius = 30;

    public DisguiseRadiusCommand(int maxRadius) {
        this.maxRadius = maxRadius;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage(ChatColor.RED + "You may not use this command from the console!");
            return true;
        }
        ArrayList<String> allowedDisguises = getAllowedDisguises(sender, "disguiseplayer");
        if (allowedDisguises.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "You are forbidden to use this command.");
            return true;
        }
        if (args.length == 0) {
            sendCommandUsage(sender);
            return true;
        }
        if (args.length == 1) {
            sender.sendMessage(ChatColor.RED + "You need to supply a disguise as well as the radius");
            return true;
        }
        if (!isNumeric(args[0])) {
            sender.sendMessage(ChatColor.RED + args[0] + " is not a number");
            return true;
        }
        int radius = Integer.parseInt(args[0]);
        if (radius > maxRadius) {
            sender.sendMessage(ChatColor.RED + "Limited radius to " + maxRadius + "! Don't want to make too much lag right?");
            radius = maxRadius;
        }
        try {
            String[] newArgs = new String[args.length - 1];
            for (int i = 0; i < newArgs.length; i++) {
                newArgs[i] = args[i + 1];
            }
            Disguise disguise = parseDisguise(sender, newArgs);
            // Time to use it!
            int disguisedEntitys = 0;
            for (Entity entity : ((Player) sender).getNearbyEntities(radius, radius, radius)) {
                if (entity == sender)
                    continue;
                DisguiseAPI.disguiseToAll(entity, disguise);
                disguisedEntitys++;
            }
            sender.sendMessage(ChatColor.RED + "Successfully disguised " + disguisedEntitys + " entities!");
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
        ArrayList<String> allowedDisguises = getAllowedDisguises(sender, "disguiseradius");
        sender.sendMessage(ChatColor.DARK_GREEN + "Disguise all entities in a radius! Caps at 30 blocks!");
        sender.sendMessage(ChatColor.DARK_GREEN + "You can use the disguises: " + ChatColor.GREEN
                + StringUtils.join(allowedDisguises, ChatColor.RED + ", " + ChatColor.GREEN));
        if (allowedDisguises.contains("player"))
            sender.sendMessage(ChatColor.DARK_GREEN + "/disguiseradius <Radius> player <Name>");
        sender.sendMessage(ChatColor.DARK_GREEN + "/disguiseradius <Radius> <DisguiseType> <Baby>");
        if (allowedDisguises.contains("dropped_item") || allowedDisguises.contains("falling_block"))
            sender.sendMessage(ChatColor.DARK_GREEN + "/disguiseradius <Radius> <Dropped_Item/Falling_Block> <Id> <Durability>");
    }
}
