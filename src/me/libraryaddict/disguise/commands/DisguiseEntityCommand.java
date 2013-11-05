package me.libraryaddict.disguise.commands;

import java.util.ArrayList;
import me.libraryaddict.disguise.BaseDisguiseCommand;
import me.libraryaddict.disguise.DisguiseListener;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class DisguiseEntityCommand extends BaseDisguiseCommand {

    private DisguiseListener listener;

    public DisguiseEntityCommand(DisguiseListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage(ChatColor.RED + "You may not use this command from the console!");
            return true;
        }
        try {
            Disguise disguise = parseDisguise(sender, args);
            listener.setSlap(sender.getName(), disguise);
            sender.sendMessage(ChatColor.RED + "Right click a entity in the next 10 seconds to disguise it as a "
                    + disguise.getType().toReadable() + "!");
        } catch (Exception ex) {
            if (ex.getMessage() != null) {
                sender.sendMessage(ex.getMessage());
            }
        }
        return true;
    }

    /**
     * Send the player the information
     */
    protected void sendCommandUsage(CommandSender sender) {
        ArrayList<String> allowedDisguises = getAllowedDisguises(sender, "disguiseentity");
        sender.sendMessage(ChatColor.DARK_GREEN + "Choose a disguise then slap a entity to disguise it!");
        sender.sendMessage(ChatColor.DARK_GREEN + "You can use the disguises: " + ChatColor.GREEN
                + StringUtils.join(allowedDisguises, ChatColor.RED + ", " + ChatColor.GREEN));
        if (allowedDisguises.contains("player"))
            sender.sendMessage(ChatColor.DARK_GREEN + "/disguiseentity player <Name>");
        sender.sendMessage(ChatColor.DARK_GREEN + "/disguise <DisguiseType> <Baby>");
        if (allowedDisguises.contains("dropped_item") || allowedDisguises.contains("falling_block"))
            sender.sendMessage(ChatColor.DARK_GREEN + "/disguiseentity <Dropped_Item/Falling_Block> <Id> <Durability>");
    }

}
