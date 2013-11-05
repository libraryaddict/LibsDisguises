package me.libraryaddict.disguise.commands;

import java.util.ArrayList;
import me.libraryaddict.disguise.BaseDisguiseCommand;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisguisePlayerCommand extends BaseDisguiseCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage(ChatColor.RED + "You may not use this command from the console!");
            return true;
        }
        ArrayList<String> allowedDisguises = getAllowedDisguises(sender, "disguiseplayer");
        if (allowedDisguises.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "You are forbidden to use this command.");
            return true;
        }
        if (args.length == 0) {
            sendCommandUsage(sender);
            return true;
        }
        if (args.length == 1) {
            sender.sendMessage(ChatColor.RED + "You need to supply a disguise as well as the player");
            return true;
        }
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Cannot find the player '" + args[0] + "'");
            return true;
        }
        try {
            String[] newArgs = new String[args.length - 1];
            for (int i = 0; i < newArgs.length; i++) {
                newArgs[i] = args[i + 1];
            }
            Disguise disguise = parseDisguise(sender, newArgs);
            DisguiseAPI.disguiseToAll(player, disguise);
            sender.sendMessage(ChatColor.RED + "Successfully disguised " + player.getName() + " as a "
                    + toReadable(disguise.getType().name()) + "!");
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
        ArrayList<String> allowedDisguises = getAllowedDisguises(sender, "disguiseplayer");
        sender.sendMessage(ChatColor.DARK_GREEN + "Disguise another player!");
        sender.sendMessage(ChatColor.DARK_GREEN + "You can use the disguises: " + ChatColor.GREEN
                + StringUtils.join(allowedDisguises, ChatColor.RED + ", " + ChatColor.GREEN));
        if (allowedDisguises.contains("player"))
            sender.sendMessage(ChatColor.DARK_GREEN + "/disguiseplayer <PlayerName> player <Name>");
        sender.sendMessage(ChatColor.DARK_GREEN + "/disguiseplayer <PlayerName> <DisguiseType> <Baby>");
        if (allowedDisguises.contains("dropped_item") || allowedDisguises.contains("falling_block"))
            sender.sendMessage(ChatColor.DARK_GREEN
                    + "/disguiseplayer <PlayerName> <Dropped_Item/Falling_Block> <Id> <Durability>");
    }

    private String toReadable(String name) {
        String[] split = name.split("_");
        for (int i = 0; i < split.length; i++)
            split[i] = split[i].substring(0, 1) + split[i].substring(1).toLowerCase();
        return StringUtils.join(split, " ");
    }
}
