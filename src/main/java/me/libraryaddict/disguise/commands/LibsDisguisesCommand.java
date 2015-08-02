package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.LibsDisguises;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LibsDisguisesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.DARK_GREEN
                    + "This server is running "
                    + "Lib's Disguises v."
                    + Bukkit.getPluginManager().getPlugin("LibsDisguises").getDescription().getVersion()
                    + " by libraryaddict, maintained by NavidK0.\n"
                    + "Use /libsdisguises reload to reload the config. All disguises will be blown by doing this.");
        } else if (args.length > 0) {
            if (sender.hasPermission("libsdisguises.reload")) {
                if (args[0].equalsIgnoreCase("reload")) {
                    LibsDisguises.instance.reload();
                    sender.sendMessage(ChatColor.GREEN + "[LibsDisguises] Reloaded config.");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "[LibsDisguises] That command doesn't exist!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You are forbidden to use this command.");
            }
        }
        return true;
    }
}
