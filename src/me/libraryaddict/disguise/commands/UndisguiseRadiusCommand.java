package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.utilities.TranslateType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class UndisguiseRadiusCommand implements CommandExecutor {
    private int maxRadius = 30;

    public UndisguiseRadiusCommand(int maxRadius) {
        this.maxRadius = maxRadius;
    }

    private boolean isNumeric(String string) {
        try {
            Integer.parseInt(string);
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage(
                    TranslateType.MESSAGE.get(ChatColor.RED + "You may not use this command from the console!"));
            return true;
        }
        if (sender.hasPermission("libsdisguises.undisguiseradius")) {
            int radius = maxRadius;
            if (args.length > 0) {
                if (!isNumeric(args[0])) {
                    sender.sendMessage(String.format(TranslateType.MESSAGE.get(
                            ChatColor.RED + "Error! " + ChatColor.GREEN + "%s" + ChatColor.RED + " is not a " + "number!"),
                            args[0]));
                    return true;
                }
                radius = Integer.parseInt(args[0]);
                if (radius > maxRadius) {
                    sender.sendMessage(String.format(TranslateType.MESSAGE.get(
                            ChatColor.RED + "Limited radius to %s" + "! Don't want to make too much lag right?"),
                            maxRadius));
                    radius = maxRadius;
                }
            }

            int disguisedEntitys = 0;
            for (Entity entity : ((Player) sender).getNearbyEntities(radius, radius, radius)) {
                if (entity == sender) {
                    continue;
                }
                if (DisguiseAPI.isDisguised(entity)) {
                    DisguiseAPI.undisguiseToAll(entity);
                    disguisedEntitys++;
                }
            }
            sender.sendMessage(
                    String.format(TranslateType.MESSAGE.get(ChatColor.RED + "Successfully undisguised %s entities!"),
                            disguisedEntitys));
        } else {
            sender.sendMessage(TranslateType.MESSAGE.get(ChatColor.RED + "You are forbidden to use this command."));
        }
        return true;
    }
}
