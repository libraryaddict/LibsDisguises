package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DisguiseModifyPlayerCommand extends DisguiseBaseCommand implements TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        DisguisePermissions permissions = getPermissions(sender);

        if (!permissions.hasPermissions()) {
            sender.sendMessage(LibsMsg.NO_PERM.get());
            return true;
        }

        if (args.length == 0) {
            sendCommandUsage(sender, permissions);
            return true;
        }

        Entity entityTarget = Bukkit.getPlayer(args[0]);

        if (entityTarget == null) {
            if (args[0].contains("-")) {
                try {
                    entityTarget = Bukkit.getEntity(UUID.fromString(args[0]));
                }
                catch (Exception ignored) {
                }
            }
        }

        if (entityTarget == null) {
            sender.sendMessage(LibsMsg.CANNOT_FIND_PLAYER.get(args[0]));
            return true;
        }

        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);

        if (newArgs.length == 0) {
            sendCommandUsage(sender, permissions);
            return true;
        }

        Disguise disguise = null;

        if (sender instanceof Player)
            disguise = DisguiseAPI.getDisguise((Player) sender, entityTarget);

        if (disguise == null)
            disguise = DisguiseAPI.getDisguise(entityTarget);

        if (disguise == null) {
            sender.sendMessage(LibsMsg.DMODPLAYER_NODISGUISE.get(entityTarget.getName()));
            return true;
        }

        DisguisePerm disguisePerm = new DisguisePerm(disguise.getType());

        if (!permissions.isAllowedDisguise(disguisePerm)) {
            sender.sendMessage(LibsMsg.DMODPLAYER_NOPERM.get());
            return true;
        }

        String[] options = DisguiseUtilities.split(StringUtils.join(newArgs, " "));

        options = DisguiseParser.parsePlaceholders(options, sender, entityTarget);

        try {
            DisguiseParser.callMethods(sender, disguise, permissions, disguisePerm, new ArrayList<>(), options,
                    "DisguiseModifyPlayer");
        }
        catch (DisguiseParseException ex) {
            if (ex.getMessage() != null) {
                sender.sendMessage(ex.getMessage());
            }
            return true;
        }

        catch (Exception ex) {
            ex.printStackTrace();
            return true;
        }

        sender.sendMessage(LibsMsg.DMODPLAYER_MODIFIED.get(entityTarget.getName()));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        ArrayList<String> tabs = new ArrayList<>();
        String[] args = getPreviousArgs(origArgs);

        DisguisePermissions perms = getPermissions(sender);

        if (!perms.hasPermissions()) {
            return tabs;
        }

        if (args.length == 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // If command user cannot see player online, don't tab-complete name
                if (sender instanceof Player && !((Player) sender).canSee(player)) {
                    continue;
                }

                tabs.add(player.getName());
            }
        } else {
            Player player = Bukkit.getPlayer(args[0]);

            if (player == null) {
                return tabs;
            }

            Disguise disguise = null;

            if (sender instanceof Player)
                disguise = DisguiseAPI.getDisguise((Player) sender, player);

            if (disguise == null)
                disguise = DisguiseAPI.getDisguise(player);

            if (disguise == null) {
                return tabs;
            }

            DisguisePerm disguiseType = new DisguisePerm(disguise.getType());

            tabs.addAll(getTabDisguiseOptions(sender, perms, disguiseType, args, 1, getCurrentArg(args)));
        }

        return filterTabs(tabs, origArgs);
    }

    /**
     * Send the player the information
     */
    @Override
    protected void sendCommandUsage(CommandSender sender, DisguisePermissions permissions) {
        ArrayList<String> allowedDisguises = getAllowedDisguises(permissions);

        sender.sendMessage(LibsMsg.DMODPLAYER_HELP1.get());
        sender.sendMessage(LibsMsg.DMODIFY_HELP3
                .get(ChatColor.GREEN + StringUtils.join(allowedDisguises, ChatColor.RED + ", " + ChatColor.GREEN)));
    }
}
