package me.libraryaddict.disguise.commands.undisguise;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
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
        if (sender instanceof Player && !sender.isOp() &&
                (!LibsPremium.isPremium() || LibsPremium.getPaidInformation() == LibsPremium.getPluginInformation())) {
            sender.sendMessage(ChatColor.RED + "This is the free version of Lib's Disguises, player commands are limited to console and Operators only! Purchase the plugin for non-admin usage!");
            return true;
        }

        if (sender.getName().equals("CONSOLE")) {
            LibsMsg.NO_CONSOLE.send(sender);
            return true;
        }

        if (sender.hasPermission("libsdisguises.undisguiseradius")) {
            int radius = maxRadius;
            if (args.length > 0) {
                if (!isNumeric(args[0])) {
                    LibsMsg.NOT_NUMBER.send(sender, args[0]);
                    return true;
                }
                radius = Integer.parseInt(args[0]);
                if (radius > maxRadius) {
                    LibsMsg.LIMITED_RADIUS.send(sender, maxRadius);
                    radius = maxRadius;
                }
            }

            Location center;

            if (sender instanceof Player) {
                center = ((Player) sender).getLocation();
            } else {
                center = ((BlockCommandSender) sender).getBlock().getLocation().add(0.5, 0, 0.5);
            }

            int disguisedEntitys = 0;

            for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                if (entity == sender) {
                    continue;
                }

                if (DisguiseAPI.isDisguised(entity)) {
                    DisguiseAPI.undisguiseToAll(entity);
                    disguisedEntitys++;
                }
            }

            LibsMsg.UNDISRADIUS.send(sender, disguisedEntitys);
        } else {
            LibsMsg.NO_PERM.send(sender);
        }
        return true;
    }
}
