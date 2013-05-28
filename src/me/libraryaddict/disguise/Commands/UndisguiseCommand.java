package me.libraryaddict.disguise.Commands;

import me.libraryaddict.disguise.DisguiseAPI;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UndisguiseCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if (sender.hasPermission("libsdisguises.undisguise")) {
            if (DisguiseAPI.isDisguised(p.getName())) {
                DisguiseAPI.undisguiseToAll(p);
                sender.sendMessage(ChatColor.RED + "You are no longer disguised");
            } else
                sender.sendMessage(ChatColor.RED + "You are not disguised!");
        } else
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
        return true;
    }
}
