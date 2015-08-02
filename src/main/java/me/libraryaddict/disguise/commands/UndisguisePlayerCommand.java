package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.DisguiseAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UndisguisePlayerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("libsdisguises.undisguiseplayer")) {
            if (args.length > 0) {
                Player p = Bukkit.getPlayer(args[0]);
                if (p != null) {
                    if (DisguiseAPI.isDisguised(p)) {
                        DisguiseAPI.undisguiseToAll(p);
                        sender.sendMessage(ChatColor.RED + "The player is no longer disguised");
                    } else {
                        sender.sendMessage(ChatColor.RED + "The player is not disguised!");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Player not found");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "/undisguiseplayer <Name>");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You are forbidden to use this command.");
        }
        return true;
    }
}
