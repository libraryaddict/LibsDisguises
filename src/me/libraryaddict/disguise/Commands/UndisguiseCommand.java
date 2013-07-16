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
        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage(ChatColor.RED + "You may not use this command from the console!");
            return true;
        }
        if (sender.hasPermission("libsdisguises.undisguise")) {
            if (DisguiseAPI.isDisguised(sender.getName())) {
                DisguiseAPI.undisguiseToAll((Player) sender);
                sender.sendMessage(ChatColor.RED + "You are no longer disguised");
            } else
                sender.sendMessage(ChatColor.RED + "You are not disguised!");
        } else
            sender.sendMessage(ChatColor.RED + "You are forbidden to use this command!");
        return true;
    }
}
