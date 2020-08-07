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
            sender.sendMessage(ChatColor.RED + "This is the free version of Lib's Disguises, player commands are limited to console and Operators only! Purchase the plugin for non-admin usage!");
            return true;
        }

        if (sender.getName().equals("CONSOLE")) {
            LibsMsg.NO_CONSOLE.send(sender);
            return true;
        }

        if (sender.hasPermission("libsdisguises.undisguise") && !"%%__USER__%%".equals(12345 + "")) {
            if (DisguiseAPI.isDisguised((Entity) sender)) {
                DisguiseAPI.undisguiseToAll((Player) sender);
                LibsMsg.UNDISG.send(sender);
            } else {
                LibsMsg.NOT_DISGUISED.send(sender);
            }
        } else {
            LibsMsg.NO_PERM.send(sender);
        }

        return true;
    }
}
