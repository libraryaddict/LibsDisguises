package me.libraryaddict.disguise.commands;

import java.util.ArrayList;
import me.libraryaddict.disguise.BaseDisguiseCommand;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisguiseCommand extends BaseDisguiseCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage(ChatColor.RED + "You may not use this command from the console!");
            return true;
        }
        Disguise disguise = null;
        try {
            disguise = parseDisguise(sender, args);
        } catch (Exception ex) {
            if (ex.getMessage() != null && !ChatColor.getLastColors(ex.getMessage()).equals("")) {
                sender.sendMessage(ex.getMessage());
            } else {
                ex.printStackTrace();
            }
            return true;
        }
        DisguiseAPI.disguiseToAll((Player) sender, disguise);
        sender.sendMessage(ChatColor.RED + "Now disguised as a " + disguise.getType().toReadable());
        return true;
    }

    /**
     * Send the player the information
     */
    protected void sendCommandUsage(CommandSender sender) {
        ArrayList<String> allowedDisguises = getAllowedDisguises(sender);
        sender.sendMessage(ChatColor.DARK_GREEN + "Choose a disguise to become the disguise!");
        sender.sendMessage(ChatColor.DARK_GREEN + "You can use the disguises: " + ChatColor.GREEN
                + StringUtils.join(allowedDisguises, ChatColor.RED + ", " + ChatColor.GREEN));
        if (allowedDisguises.contains("player"))
            sender.sendMessage(ChatColor.DARK_GREEN + "/disguise player <Name>");
        sender.sendMessage(ChatColor.DARK_GREEN + "/disguise <DisguiseType> <Baby>");
        if (allowedDisguises.contains("dropped_item") || allowedDisguises.contains("falling_block"))
            sender.sendMessage(ChatColor.DARK_GREEN + "/disguiseplayer <Dropped_Item/Falling_Block> <Id> <Durability>");
    }
}
