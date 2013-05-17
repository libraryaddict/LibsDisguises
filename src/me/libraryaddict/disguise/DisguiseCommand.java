package me.libraryaddict.disguise;

import java.util.ArrayList;
import java.util.Collections;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import me.libraryaddict.disguise.DisguiseTypes.DisguiseType;
import me.libraryaddict.disguise.DisguiseTypes.MiscDisguise;
import me.libraryaddict.disguise.DisguiseTypes.MobDisguise;
import me.libraryaddict.disguise.DisguiseTypes.PlayerDisguise;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisguiseCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.isOp()) {
            Player p = (Player) sender;
            if (args.length == 0) {
                ArrayList<String> names = new ArrayList<String>();
                for (DisguiseType type : DisguiseType.values()) {
                    names.add(type.name().toLowerCase());
                }
                Collections.sort(names);
                sender.sendMessage(ChatColor.RED + "You can use the disguises: " + ChatColor.GREEN
                        + StringUtils.join(names, ChatColor.RED + ", " + ChatColor.GREEN));
            } else if (args[0].equalsIgnoreCase("undiguise") || args[0].equalsIgnoreCase("undis")
                    || args[0].equalsIgnoreCase("un")) {
                if (DisguiseAPI.isDisguised(p.getName())) {
                    DisguiseAPI.undisguiseToAll(p);
                    sender.sendMessage(ChatColor.RED + "You are no longer disguised");
                } else
                    sender.sendMessage(ChatColor.RED + "You are not disguised!");
            } else if (args[0].equalsIgnoreCase("player")) {
                if (args.length > 1) {
                    String name = ChatColor.translateAlternateColorCodes('&',
                            StringUtils.join(args, " ").substring(args[0].length() + 1));
                    PlayerDisguise disguise = new PlayerDisguise(name);
                    DisguiseAPI.disguiseToAll(p, disguise);
                    sender.sendMessage(ChatColor.RED + "Now disguised as the player '" + ChatColor.GREEN + name + ChatColor.RESET
                            + ChatColor.RED + "'");
                } else
                    sender.sendMessage(ChatColor.RED + "You need to provide a player name");
            } else {
                DisguiseType type;
                try {
                    type = DisguiseType.valueOf(args[0].toUpperCase());
                } catch (Exception ex) {
                    sender.sendMessage(ChatColor.RED + "Failed to find disguise: " + ChatColor.GREEN + args[0]
                            + "\n/disguise player <Name>\n/disguise <Mob Name>\n/disguise undisguise/un/undis");
                    return true;
                }
                boolean adult = true;
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("true")) {
                        adult = false;
                    } else if (!args[1].equalsIgnoreCase("false")) {
                        sender.sendMessage(ChatColor.RED + "Set baby: " + ChatColor.GREEN + args[1] + ChatColor.RED
                                + " - Thats not true or false..");
                        return true;
                    }
                }
                Disguise disguise;
                if (type.isMob())
                    disguise = new MobDisguise(type, adult);
                else
                    disguise = new MiscDisguise(type);
                DisguiseAPI.disguiseToAll(p, disguise);
                sender.sendMessage(ChatColor.RED + "Now disguised as a " + type.name().toLowerCase() + "!");
            }
        } else
            sender.sendMessage(ChatColor.RED + "You do not have permission");
        return true;
    }
}
