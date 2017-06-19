package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.LibsMsg;
import me.libraryaddict.disguise.utilities.TranslateType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UndisguiseEntityCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage(LibsMsg.NO_CONSOLE.get());
            return true;
        }
        if (sender.hasPermission("libsdisguises.undisguiseentity")) {
            LibsDisguises.getInstance().getListener().setDisguiseEntity(sender.getName(), null);
            sender.sendMessage(LibsMsg.UND_ENTITY.get());
        } else {
            sender.sendMessage(LibsMsg.NO_PERM.get());
        }
        return true;
    }
}
