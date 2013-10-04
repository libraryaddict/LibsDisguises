package me.libraryaddict.disguise.commands;


import me.libraryaddict.disguise.DisguiseListener;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class UndisguiseEntityCommand implements CommandExecutor {
    private DisguiseListener listener;

    public UndisguiseEntityCommand(DisguiseListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage(ChatColor.RED + "You may not use this command from the console!");
            return true;
        }
        if (sender.hasPermission("libsdisguises.undisguiseentity")) {
            listener.setSlap(sender.getName(), null);
        } else
            sender.sendMessage(ChatColor.RED + "You are forbidden to use this command!");
        return true;
    }
}
