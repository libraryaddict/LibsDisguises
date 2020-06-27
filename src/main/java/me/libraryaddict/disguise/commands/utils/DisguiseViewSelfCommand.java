package me.libraryaddict.disguise.commands.utils;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Navid
 */
public class DisguiseViewSelfCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.getName().equals("CONSOLE")) {
            DisguiseUtilities.sendMessage(sender, LibsMsg.NO_CONSOLE);
            return true;
        }

        Player player = (Player) sender;

        if (DisguiseAPI.isViewSelfToggled(player)) {
            DisguiseAPI.setViewDisguiseToggled(player, false);
            DisguiseUtilities.sendMessage(sender, LibsMsg.VIEW_SELF_OFF);
        } else {
            DisguiseAPI.setViewDisguiseToggled(player, true);
            DisguiseUtilities.sendMessage(sender, LibsMsg.VIEW_SELF_ON);
        }

        return true;
    }
}
