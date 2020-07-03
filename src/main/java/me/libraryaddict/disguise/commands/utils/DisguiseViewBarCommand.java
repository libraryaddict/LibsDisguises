package me.libraryaddict.disguise.commands.utils;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
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
            DisguiseUtilities.sendMessage(sender, LibsMsg.NO_CONSOLE);
            return true;
        }

        Player player = (Player) sender;

        if (DisguiseAPI.isViewBarToggled(player)) {
            DisguiseAPI.setViewBarToggled(player, false);
            DisguiseUtilities.sendMessage(sender, LibsMsg.VIEW_BAR_OFF);
        } else {
            DisguiseAPI.setViewBarToggled(player, true);
            DisguiseUtilities.sendMessage(sender, LibsMsg.VIEW_BAR_ON);
        }

        return true;
    }
}
