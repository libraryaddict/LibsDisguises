package me.libraryaddict.disguise.Commands;

import java.util.ArrayList;
import java.util.Collections;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import me.libraryaddict.disguise.DisguiseTypes.DisguiseType;
import me.libraryaddict.disguise.DisguiseTypes.MiscDisguise;
import me.libraryaddict.disguise.DisguiseTypes.MobDisguise;
import me.libraryaddict.disguise.DisguiseTypes.PlayerDisguise;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisguisePlayerCommand implements CommandExecutor {

    private ArrayList<String> allowedDisguises(CommandSender sender) {
        ArrayList<String> names = new ArrayList<String>();
        for (DisguiseType type : DisguiseType.values()) {
            String name = type.name().toLowerCase();
            if (sender.hasPermission("libsdisguises.disguiseothers." + name))
                names.add(name);
        }
        Collections.sort(names);
        return names;
    }

    private ArrayList<String> forbiddenDisguises(CommandSender sender) {
        ArrayList<String> names = new ArrayList<String>();
        for (DisguiseType type : DisguiseType.values()) {
            String name = type.name().toLowerCase();
            if (!sender.hasPermission("libsdisguises.disguiseothers." + name))
                names.add(name);
        }
        Collections.sort(names);
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
        if (sender.hasPermission("libsdisguises.disguiseothers")
                || (args.length > 0 && args[0].toLowerCase().startsWith("un") && sender
                        .hasPermission("libsdisguises.undisguiseothers"))) {
            if (args.length == 0) {
                ArrayList<String> names = allowedDisguises(sender);
                ArrayList<String> otherNames = forbiddenDisguises(sender);
                if (names.size() > 0) {
                    sender.sendMessage(ChatColor.RED + "You can use the disguises: " + ChatColor.GREEN
                            + StringUtils.join(names, ChatColor.RED + ", " + ChatColor.GREEN));
                    if (otherNames.size() > 0) {
                        sender.sendMessage(ChatColor.RED + "Other disguises: " + ChatColor.GREEN
                                + StringUtils.join(names, ChatColor.RED + ", " + ChatColor.GREEN));
                    }
                } else
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            } else {
                if (args.length > 1) {
                    Player p = Bukkit.getPlayer(args[0]);
                    if (p != null) {
                        if (args[1].equalsIgnoreCase("undiguise") || args[1].equalsIgnoreCase("undis")
                                || args[1].equalsIgnoreCase("un")) {
                            if (sender.hasPermission("libsdisguises.undisguiseothers")) {
                                if (DisguiseAPI.isDisguised(p.getName())) {
                                    DisguiseAPI.undisguiseToAll(p);
                                    sender.sendMessage(ChatColor.RED + "They are no longer disguised");
                                } else
                                    sender.sendMessage(ChatColor.RED + "They are not disguised!");
                            } else
                                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                        } else if (args[1].equalsIgnoreCase("player")) {
                            if (sender.hasPermission("libsdisguises.disguiseothers.player")) {
                                if (args.length > 2) {
                                    String name = ChatColor.translateAlternateColorCodes('&', StringUtils.join(args, " ")
                                            .substring(args[0].length() + args[1].length() + 2));
                                    PlayerDisguise disguise = new PlayerDisguise(name);
                                    DisguiseAPI.disguiseToAll(p, disguise);
                                    sender.sendMessage(ChatColor.RED + "Disguised " + p.getName() + " as the player '"
                                            + ChatColor.GREEN + name + ChatColor.RESET + ChatColor.RED + "'");
                                } else
                                    sender.sendMessage(ChatColor.RED + "You need to provide a player name");
                            } else {
                                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
                            }

                        } else {
                            if (allowedDisguises(sender).size() == 0) {
                                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                                return true;
                            }
                            DisguiseType type;
                            try {
                                type = DisguiseType.valueOf(args[1].toUpperCase());
                            } catch (Exception ex) {
                                sender.sendMessage(ChatColor.RED
                                        + "Failed to find disguise: "
                                        + ChatColor.GREEN
                                        + args[1]
                                        + "\n/disguiseplayer <Name> player <Name>\n/disguiseplayer <Name><Mob Name>\n/disguiseplayer <Name> undisguise/un/undis\n/undisguiseplayer");
                                return true;
                            }
                            if (sender.hasPermission("libsdisguises.disguiseothers." + type.name().toLowerCase())) {
                                Object args1 = true;
                                if (type.isMisc())
                                    args1 = -1;
                                int args2 = -1;
                                if (args.length > 2) {
                                    if (type.isMob()) {
                                        if (args[2].equalsIgnoreCase("true")) {
                                            args1 = false;
                                        } else if (!args[2].equalsIgnoreCase("false")) {
                                            sender.sendMessage(ChatColor.RED + "Set baby: " + ChatColor.GREEN + args[2]
                                                    + ChatColor.RED + " - Thats not true or false..");
                                            return true;
                                        }
                                    } else if (type.isMisc()) {
                                        if (isNumeric(args[2])) {
                                            args1 = Integer.parseInt(args[2]);
                                        } else {
                                            sender.sendMessage(ChatColor.RED + args[2] + " is not a number");
                                            return true;
                                        }
                                        if (args.length > 3)
                                            if (isNumeric(args[3])) {
                                                args2 = Integer.parseInt(args[3]);
                                            } else {
                                                sender.sendMessage(ChatColor.RED + args[3] + " is not a number");
                                                return true;
                                            }
                                    }
                                }
                                Disguise disguise;
                                if (type.isMob())
                                    disguise = new MobDisguise(type, (Boolean) args1);
                                else
                                    disguise = new MiscDisguise(type, (Integer) args1, args2);
                                DisguiseAPI.disguiseToAll(p, disguise);
                                sender.sendMessage(ChatColor.RED + "Disguised " + p.getName() + " as a "
                                        + type.name().toLowerCase() + "!");
                            } else
                                sender.sendMessage(ChatColor.RED + "You do not have permission to use this disguise.");
                        }
                    } else
                        sender.sendMessage(ChatColor.RED + "Player not found");
                } else
                    sender.sendMessage(ChatColor.RED + "/disguiseplayer <Name> <Disguise>");
            }
        } else
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
        return true;
    }
}
