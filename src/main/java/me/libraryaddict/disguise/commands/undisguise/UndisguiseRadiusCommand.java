package me.libraryaddict.disguise.commands.undisguise;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
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
        if (sender instanceof Player && !sender.isOp() &&
                (!LibsPremium.isPremium() || LibsPremium.getPaidInformation() == LibsPremium.getPluginInformation())) {
            sender.sendMessage(ChatColor.RED + "Please purchase Lib's Disguises to enable player commands");
            return true;
        }

        if (sender.getName().equals("CONSOLE")) {
            DisguiseUtilities.sendMessage(sender, LibsMsg.NO_CONSOLE);
            return true;
        }

        if (sender.hasPermission("libsdisguises.undisguiseradius")) {
            int radius = maxRadius;
            if (args.length > 0) {
                if (!isNumeric(args[0])) {
                    DisguiseUtilities.sendMessage(sender, LibsMsg.NOT_NUMBER, args[0]);
                    return true;
                }
                radius = Integer.parseInt(args[0]);
                if (radius > maxRadius) {
                    DisguiseUtilities.sendMessage(sender, LibsMsg.LIMITED_RADIUS, maxRadius);
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

            DisguiseUtilities.sendMessage(sender, LibsMsg.UNDISRADIUS, disguisedEntitys);
        } else {
            DisguiseUtilities.sendMessage(sender, LibsMsg.NO_PERM);
        }
        return true;
    }
}
