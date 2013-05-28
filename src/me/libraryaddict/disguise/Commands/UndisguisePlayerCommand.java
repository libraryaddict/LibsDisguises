package me.libraryaddict.disguise.Commands;

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
        if (sender.hasPermission("libsdisguises.undisguiseothers")) {
            if (args.length > 0) {
                Player p = Bukkit.getPlayer(args[0]);
                if (p != null) {
                    if (DisguiseAPI.isDisguised(p.getName())) {
                        DisguiseAPI.undisguiseToAll(p);
                        sender.sendMessage(ChatColor.RED + "He is no longer disguised");
                    } else
                        sender.sendMessage(ChatColor.RED + "He is not disguised!");
                } else
                    sender.sendMessage(ChatColor.RED + "Player not found");
            } else
                sender.sendMessage(ChatColor.RED + "/undisguiseplayer <Name>");
        } else
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
        return true;
    }
}
