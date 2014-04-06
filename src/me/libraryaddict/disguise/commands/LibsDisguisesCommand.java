package me.libraryaddict.disguise.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LibsDisguisesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "Lib's Disguises was created by libraryaddict");
        if (sender.getName().equals("libraryaddict")) {
            sender.sendMessage(ChatColor.GRAY + "The server is running version "
                    + Bukkit.getPluginManager().getPlugin("LibsDisguises").getDescription().getVersion());
        }
        return true;
    }
}
