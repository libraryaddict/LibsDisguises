package me.libraryaddict.disguise.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LibsDisguisesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(ChatColor.DARK_GREEN
                + "This server is running "
                + "Lib's Disguises "
                + (sender.getName().equals("libraryaddict") ? "v"
                        + Bukkit.getPluginManager().getPlugin("LibsDisguises").getDescription().getVersion() + " " : "")
                + "by libraryaddict");
        return true;
    }
}
