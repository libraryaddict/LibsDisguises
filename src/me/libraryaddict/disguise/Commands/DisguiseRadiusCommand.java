package me.libraryaddict.disguise.commands;

import java.util.ArrayList;
import java.util.Collections;


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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


public class DisguiseRadiusCommand implements CommandExecutor {
    private int maxRadius = 30;

    public DisguiseRadiusCommand(int maxRadius) {
        this.maxRadius = maxRadius;
    }

    private ArrayList<String> allowedDisguises(CommandSender sender) {
        ArrayList<String> names = new ArrayList<String>();
        for (DisguiseType type : DisguiseType.values()) {
            String name = type.name().toLowerCase();
            if (sender.hasPermission("libsdisguises.disguiseradius.*")
                    || sender.hasPermission("libsdisguises.disguiseradius." + name))
                names.add(name);
        }
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    private ArrayList<String> forbiddenDisguises(CommandSender sender) {
        ArrayList<String> names = new ArrayList<String>();
        if (sender.hasPermission("libsdisguises.disguiseradius.*"))
            return names;
        for (DisguiseType type : DisguiseType.values()) {
            String name = type.name().toLowerCase();
            if (!sender.hasPermission("libsdisguises.disguiseradius." + name))
                names.add(name);
        }
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    private boolean isNumeric(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage(ChatColor.RED + "You may not use this command from the console!");
            return true;
        }
        // What disguises can he use
        ArrayList<String> allowedDisguises = allowedDisguises(sender);
        // If he owns at least one disguise
        if (allowedDisguises.size() > 0) {
            // Get his forbidden disguises (Disguises he can't use) for later use
            ArrayList<String> forbiddenDisguises = forbiddenDisguises(sender);
            // If he is attempting to do something
            if (args.length > 0) {// Better go check that its a proper radius
                if (!isNumeric(args[0])) {// Radius doesn't exist. Knew it!
                    sender.sendMessage(ChatColor.RED + "Error! Radius " + ChatColor.GREEN + args[0] + ChatColor.RED
                            + " isn't a number!");
                    return true;
                }
                if (args.length > 1) {
                    // If he owns the disguise
                    if (allowedDisguises.contains(args[1].toLowerCase())) {
                        // He can use the disguise huh.
                        int radius = Integer.parseInt(args[0]);
                        if (radius > maxRadius) {
                            sender.sendMessage(ChatColor.RED + "Limited radius to " + maxRadius
                                    + "! Don't want to make too much lag right?");
                            radius = maxRadius;
                        }
                        Disguise disguise = null;
                        // Time to start constructing the disguise.
                        // We will need to check between all 3 kinds of disguises
                        if (args[1].equalsIgnoreCase("player")) { // If he is doing a player disguise
                            // Did he give enough args?
                            if (args.length == 2) {
                                // He needs to give the player name
                                sender.sendMessage(ChatColor.RED + "Error! You need to give a player name!");
                                return true;
                            } else {
                                // Construct the player disguise
                                disguise = new PlayerDisguise(ChatColor.translateAlternateColorCodes('&', args[2]));
                            }
                        } else {
                            // Grab the disguise type so we know what constructor to use
                            DisguiseType disguiseType = DisguiseType.valueOf(args[1].toUpperCase());
                            if (disguiseType.isMob()) { // Its a mob, use the mob constructor
                                boolean adult = true;
                                if (args.length > 2) {
                                    // Seems they want to make this a baby disguise!
                                    if (!args[2].equalsIgnoreCase("false") && !args[2].equalsIgnoreCase("true")) {
                                        sender.sendMessage(ChatColor.RED + "Error! " + ChatColor.GREEN + args[2] + ChatColor.RED
                                                + " isn't true or false!");
                                        return true;
                                    }
                                    adult = args[2].equalsIgnoreCase("false"); // Adult = !arg
                                }
                                disguise = new MobDisguise(disguiseType, adult);
                            } else if (disguiseType.isMisc()) {
                                // Its a misc, we are going to use the MiscDisguise constructor.
                                int miscId = -1;
                                int miscData = -1;
                                if (args.length > 2) {
                                    // They have defined more arguements!
                                    // If the first arg is a number
                                    if (isNumeric(args[2])) {
                                        miscId = Integer.parseInt(args[2]);
                                    } else {
                                        // Send them a error
                                        sender.sendMessage(ChatColor.RED + "Error! " + ChatColor.GREEN + args[2] + ChatColor.RED
                                                + " is not a number!");
                                        return true;
                                    }
                                    // If they also defined a data value
                                    if (args.length > 3) {
                                        if (isNumeric(args[3])) {
                                            miscData = Integer.parseInt(args[3]);
                                        } else {
                                            // Send them a error
                                            sender.sendMessage(ChatColor.RED + "Error! " + ChatColor.GREEN + args[3]
                                                    + ChatColor.RED + " is not a number!");
                                            return true;
                                        }
                                    }
                                }
                                // Construct the disguise
                                disguise = new MiscDisguise(disguiseType, true, miscId, miscData);
                            }
                        }
                        // Alright. We've constructed our disguise.
                        // Time to use it!
                        int disguisedEntitys = 0;
                        for (Entity entity : ((Player) sender).getNearbyEntities(radius, radius, radius)) {
                            if (entity == sender)
                                continue;
                            DisguiseAPI.disguiseToAll(entity, disguise);
                            disguisedEntitys++;
                        }
                        sender.sendMessage(ChatColor.RED + "Successfully disguised " + disguisedEntitys + " entities!");
                    } else {
                        // He doesn't. Either tell him its incorrect or he isn't allowed to use it
                        if (forbiddenDisguises.contains(args[0].toLowerCase())) {
                            // He isn't allowed to use it..
                            sender.sendMessage(ChatColor.RED + "You are forbidden to use this disguise!");
                        } else {
                            sender.sendMessage(ChatColor.RED + "Error! The disguise " + ChatColor.GREEN + args[1] + ChatColor.RED
                                    + " doesn't exist!");
                        }
                    }
                } else
                    sender.sendMessage(ChatColor.RED + "Error! You need to state a disguise!");
            } else {
                // Just send the disguises information.
                sendDisguises(sender, allowedDisguises, forbiddenDisguises);
            }
        } else
            sender.sendMessage(ChatColor.RED + "You are forbidden to use this command!");
        return true;
    }

    /**
     * Send the player the information
     */
    private void sendDisguises(CommandSender sender, ArrayList<String> allowedDisguises, ArrayList<String> forbiddenDisguises) {
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
