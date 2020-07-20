package me.libraryaddict.disguise.commands.utils;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by libraryaddict on 2/07/2020.
 */
public class DisguiseViewBarCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.getName().equals("CONSOLE")) {
            LibsMsg.NO_CONSOLE.send(sender);
            return true;
        }

        Player player = (Player) sender;

        if (DisguiseAPI.isActionBarShown(player)) {
            DisguiseAPI.setActionBarShown(player, false);
            LibsMsg.VIEW_BAR_OFF.send(sender);
        } else {
            DisguiseAPI.setActionBarShown(player, true);
            LibsMsg.VIEW_BAR_ON.send(sender);
        }

        return true;
    }
}
