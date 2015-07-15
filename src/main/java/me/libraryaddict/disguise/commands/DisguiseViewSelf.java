package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.DisguiseAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Navid
 */
public class DisguiseViewSelf implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage(ChatColor.RED + "You may not use this command from the console!");
            return true;
        }
        Player player = (Player) sender;
        if (DisguiseAPI.isViewSelfToggled(player)) {
            DisguiseAPI.setViewDisguiseToggled(player, false);
            sender.sendMessage(ChatColor.GREEN + "Toggled viewing own disguise off!");
        } else {
            DisguiseAPI.setViewDisguiseToggled(player, true);
            sender.sendMessage(ChatColor.GREEN + "Toggled viewing own disguise on!");
        }
        return true;
    }

}
