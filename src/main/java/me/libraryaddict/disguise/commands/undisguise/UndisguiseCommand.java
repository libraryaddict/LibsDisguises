package me.libraryaddict.disguise.commands.undisguise;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class UndisguiseCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player && !sender.isOp() &&
                (!LibsPremium.isPremium() || LibsPremium.getPaidInformation() == LibsPremium.getPluginInformation())) {
            sender.sendMessage(ChatColor.RED + "Please purchase Lib's Disguises to enable player commands");
            return true;
        }

        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage(LibsMsg.NO_CONSOLE.get());
            return true;
        }

        if (sender.hasPermission("libsdisguises.undisguise") && !"%%__USER__%%".equals(12345 + "")) {
            if (DisguiseAPI.isDisguised((Entity) sender)) {
                DisguiseAPI.undisguiseToAll((Player) sender);
                sender.sendMessage(LibsMsg.UNDISG.get());
            } else {
                sender.sendMessage(LibsMsg.NOT_DISGUISED.get());
            }
        } else {
            sender.sendMessage(LibsMsg.NO_PERM.get());
        }

        return true;
    }
}
