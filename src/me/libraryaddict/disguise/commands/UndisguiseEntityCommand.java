package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.TranslateType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UndisguiseEntityCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage(
                    TranslateType.MESSAGE.get(ChatColor.RED + "You may not use this command from the console!"));
            return true;
        }
        if (sender.hasPermission("libsdisguises.undisguiseentity")) {
            LibsDisguises.getInstance().getListener().setDisguiseEntity(sender.getName(), null);
            sender.sendMessage(TranslateType.MESSAGE.get(
                    ChatColor.RED + "Right click a disguised entity to " + "undisguise them!"));
        } else {
            sender.sendMessage(TranslateType.MESSAGE.get(ChatColor.RED + "You are forbidden to use this command."));
        }
        return true;
    }
}
