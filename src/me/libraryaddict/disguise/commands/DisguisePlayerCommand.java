package me.libraryaddict.disguise.commands;

import java.util.ArrayList;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.BaseDisguiseCommand;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisguisePlayerCommand extends BaseDisguiseCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> allowedDisguises = getAllowedDisguises(sender);
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
        String[] newArgs = new String[args.length - 1];
        for (int i = 0; i < newArgs.length; i++) {
            newArgs[i] = args[i + 1];
        }
        Disguise disguise;
        try {
            disguise = parseDisguise(sender, newArgs);
        } catch (Exception ex) {
            if (ex.getMessage() != null && !ChatColor.getLastColors(ex.getMessage()).equals("")) {
                sender.sendMessage(ex.getMessage());
            } else if (ex.getCause() != null) {
                ex.printStackTrace();
            }
            return true;
        }
        if (disguise.isMiscDisguise() && !DisguiseConfig.isMiscDisguisesForLivingEnabled()) {
            sender.sendMessage(ChatColor.RED
                    + "Can't disguise a living entity as a misc disguise. This has been disabled in the config!");
            return true;
        }
        if (DisguiseConfig.isNameOfPlayerShownAboveDisguise()) {
            if (disguise.getWatcher() instanceof LivingWatcher) {
                ((LivingWatcher) disguise.getWatcher()).setCustomName(((Player) player).getDisplayName());
                if (DisguiseConfig.isNameAboveHeadAlwaysVisible()) {
                    ((LivingWatcher) disguise.getWatcher()).setCustomNameVisible(true);
                }
            }
        }
        DisguiseAPI.disguiseToAll(player, disguise);
        sender.sendMessage(ChatColor.RED + "Successfully disguised " + player.getName() + " as a "
                + disguise.getType().toReadable() + "!");
        return true;
    }

    /**
     * Send the player the information
     */
    protected void sendCommandUsage(CommandSender sender) {
        ArrayList<String> allowedDisguises = getAllowedDisguises(sender);
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
}
